package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.messaging.dto.CreateEventDto;
import com.kompozith.komflow.features.messaging.dto.CreateEventRegistrationWorkflowStepDto;
import com.kompozith.komflow.features.messaging.dto.EventAgendaItemDto;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.dto.EventRegistrationWorkflowStepDto;
import com.kompozith.komflow.features.messaging.entity.Event;
import com.kompozith.komflow.features.messaging.entity.EventRegistrationWorkflowStep;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowConditionType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowRecipientType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowStepType;
import com.kompozith.komflow.features.messaging.entity.EventMode;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.mapper.EventMapper;
import com.kompozith.komflow.features.messaging.repository.EventRepository;
import com.kompozith.komflow.features.messaging.repository.MessageRepository;
import com.kompozith.komflow.features.organization.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kompozith.komflow.features.messaging.dto.DailyRegistrationCountDto;
import com.kompozith.komflow.features.messaging.dto.EventRegistrationStatsDto;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final ZoneId DEFAULT_EVENT_ZONE = ZoneId.of("GMT");
    private static final String EVENT_REGISTRATION_TAG_PREFIX = "EVENT-REG-";

    private final EventRepository eventRepository;
    private final TagRepository tagRepository;
    private final ContactRepository contactRepository;
    private final MessageRepository messageRepository;
    private final EventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final EventRichTextSanitizerService eventRichTextSanitizerService;

    @Override
    @Transactional
    public EventDto create(CreateEventDto createEventDto) {
        Long orgId = TenantContext.getOrganizationId();
        validatePayload(createEventDto);
        Event event = eventMapper.createEventDtoToEvent(createEventDto);
        event.setOrganizationId(orgId);
        event.setTitle(createEventDto.getTitle().trim());
        event.setSlug(generateUniqueSlug(event.getTitle(), null));
        event.setDescription(sanitizeRichTextToNull(createEventDto.getDescription()));
        event.setLocation(trimToNull(createEventDto.getLocation()));
        event.setSubtitle(trimToNull(createEventDto.getSubtitle()));
        event.setAddress(trimToNull(createEventDto.getAddress()));
        EventMode mode = resolveMode(createEventDto.getMode());
        event.setMode(mode);
        event.setMeetingUrl(mode == EventMode.ONLINE ? trimToNull(createEventDto.getMeetingUrl()) : null);
        event.setBannerImageUrl(trimToNull(createEventDto.getBannerImageUrl()));
        event.setHighlights(serializeHighlights(createEventDto.getHighlights()));
        event.setAgenda(serializeAgenda(createEventDto.getAgenda()));
        applyDateTimePayload(event, createEventDto);
        Event saved = eventRepository.save(event);
        applyRegistrationWorkflow(saved, createEventDto.getRegistrationWorkflowSteps());
        Event savedWithWorkflow = eventRepository.save(saved);
        ensureEventRegistrationTag(savedWithWorkflow);
        return toEventDto(savedWithWorkflow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> findAll() {
        Long orgId = TenantContext.getOrganizationId();
        return eventRepository.findAll().stream()
                .filter(e -> orgId.equals(e.getOrganizationId()))
                .map(this::toEventDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> findFuture(Instant from) {
        Long orgId = TenantContext.getOrganizationId();
        Instant start = from != null ? from : Instant.now();
        return eventRepository.findAllByStartAtGreaterThanEqualOrderByStartAtAsc(start).stream()
                .filter(e -> orgId.equals(e.getOrganizationId()))
                .map(this::toEventDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> findCalendarRange(Instant start, Instant end) {
        if (start == null || end == null) {
            return findAll();
        }

        List<Event> startedInRange = eventRepository.findAllByStartAtBetweenOrderByStartAtAsc(start, end);
        List<Event> coveringRange = eventRepository.findAllByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtAsc(end, start);

        return java.util.stream.Stream.concat(startedInRange.stream(), coveringRange.stream())
                .collect(java.util.stream.Collectors.toMap(Event::getId, e -> e, (a, b) -> a))
                .values()
                .stream()
                .sorted(java.util.Comparator.comparing(Event::getStartAt))
                .map(this::toEventDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto findById(Long id) {
        Event event = findEntityById(id);
        return toEventDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Event slug is required");
        }
        Event event = eventRepository.findBySlug(slug.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ObjectNotFoundException(Event.class.getSimpleName(), "slug", slug));
        return toEventDto(event);
    }

    @Override
    @Transactional
    public EventDto update(Long id, CreateEventDto createEventDto) {
        validatePayload(createEventDto);
        Event event = findEntityById(id);

        String normalizedTitle = createEventDto.getTitle().trim();
        event.setTitle(normalizedTitle);
        event.setSlug(generateUniqueSlug(normalizedTitle, event.getId()));
        event.setDescription(sanitizeRichTextToNull(createEventDto.getDescription()));
        event.setLocation(trimToNull(createEventDto.getLocation()));
        event.setSubtitle(trimToNull(createEventDto.getSubtitle()));
        event.setAddress(trimToNull(createEventDto.getAddress()));
        EventMode mode = resolveMode(createEventDto.getMode());
        event.setMode(mode);
        event.setMeetingUrl(mode == EventMode.ONLINE ? trimToNull(createEventDto.getMeetingUrl()) : null);
        event.setBannerImageUrl(trimToNull(createEventDto.getBannerImageUrl()));
        event.setHighlights(serializeHighlights(createEventDto.getHighlights()));
        event.setAgenda(serializeAgenda(createEventDto.getAgenda()));
        applyDateTimePayload(event, createEventDto);

        applyRegistrationWorkflow(event, createEventDto.getRegistrationWorkflowSteps());
        Event saved = eventRepository.save(event);
        ensureEventRegistrationTag(saved);
        return toEventDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Event.class.getSimpleName(), id));

        // Prevent FK violations and keep messages reusable after event deletion.
        messageRepository.detachEventReferences(id);
        deleteEventRegistrationTags(id);
        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Event findEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Event.class.getSimpleName(), id));
    }

    private void validatePayload(CreateEventDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Event payload is required");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event title is required");
        }
        EventMode mode = resolveMode(dto.getMode());
        String meetingUrl = trimToNull(dto.getMeetingUrl());
        String location = trimToNull(dto.getLocation());
        if (mode == EventMode.ONLINE && meetingUrl == null) {
            throw new IllegalArgumentException("Meeting URL is required for online events");
        }
        if (mode == EventMode.ONSITE && location == null) {
            throw new IllegalArgumentException("Location is required for onsite events");
        }
        if (dto.getStartDate() == null) {
            throw new IllegalArgumentException("Event start date is required");
        }
        if (dto.getEndDate() == null) {
            throw new IllegalArgumentException("Event end date is required");
        }
        if (dto.getStartTime() == null) {
            throw new IllegalArgumentException("Event start time is required");
        }
        if (dto.getEndTime() == null) {
            throw new IllegalArgumentException("Event end time is required");
        }

        String timezone = normalizeTimezone(dto.getTimezone());
        Instant startAt = resolveStartAt(dto, timezone);
        if (startAt == null) {
            throw new IllegalArgumentException("Event start date/time is required");
        }

        Instant endAt = resolveEndAt(dto, timezone);
        if (endAt == null) {
            throw new IllegalArgumentException("Event end date/time is required");
        }
        if (endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("Event end date/time must be greater than or equal to start date/time");
        }
    }

    private void applyDateTimePayload(Event event, CreateEventDto dto) {
        String timezone = normalizeTimezone(dto.getTimezone());
        Instant startAt = resolveStartAt(dto, timezone);
        Instant endAt = resolveEndAt(dto, timezone);

        event.setTimezone(timezone);
        event.setStartAt(startAt);
        event.setEndAt(endAt);
    }

    private Instant resolveStartAt(CreateEventDto dto, String timezone) {
        if (dto.getStartAt() != null) {
            return dto.getStartAt();
        }

        if (dto.getStartDate() == null || dto.getStartTime() == null) {
            return null;
        }
        return ZonedDateTime.of(dto.getStartDate(), dto.getStartTime(), resolveZoneId(timezone)).toInstant();
    }

    private Instant resolveEndAt(CreateEventDto dto, String timezone) {
        if (dto.getEndAt() != null) {
            return dto.getEndAt();
        }

        if (dto.getEndDate() == null || dto.getEndTime() == null) {
            return null;
        }
        return ZonedDateTime.of(dto.getEndDate(), dto.getEndTime(), resolveZoneId(timezone)).toInstant();
    }

    private EventDto toEventDto(Event event) {
        EventDto dto = eventMapper.eventToEventDto(event);
        if (dto == null || event == null) {
            return dto;
        }

        ZoneId zoneId = resolveZoneId(event.getTimezone());
        if (event.getStartAt() != null) {
            ZonedDateTime start = event.getStartAt().atZone(zoneId);
            dto.setEventDate(start.toLocalDate());
            dto.setStartDate(start.toLocalDate());
            dto.setStartTime(start.toLocalTime().withSecond(0).withNano(0));
        }
        if (event.getEndAt() != null) {
            ZonedDateTime end = event.getEndAt().atZone(zoneId);
            dto.setEndDate(end.toLocalDate());
            dto.setEndTime(end.toLocalTime().withSecond(0).withNano(0));
        }

        dto.setHighlights(parseHighlights(event.getHighlights()));
        dto.setAgenda(parseAgenda(event.getAgenda()));
        dto.setTimezone(normalizeTimezone(event.getTimezone()));
        dto.setRegistrationWorkflowSteps(mapWorkflowSteps(event.getRegistrationWorkflowSteps()));
        return dto;
    }

    private List<EventRegistrationWorkflowStepDto> mapWorkflowSteps(List<EventRegistrationWorkflowStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }
        return steps.stream()
                .filter(step -> step != null)
                .map(step -> {
                    Message message = step.getMessage();
                    return new EventRegistrationWorkflowStepDto(
                            step.getId(),
                            message != null ? message.getId() : null,
                            message != null ? message.getTitle() : null,
                            message != null ? message.getChannel() : null,
                            step.getStepType(),
                            step.getRecipientType(),
                            step.getDelayMinutes(),
                            step.getConditionType(),
                            step.getConditionValue(),
                            step.getPosition(),
                            step.isEnabled(),
                            step.getRecipientEmails()
                    );
                })
                .toList();
    }

    private void applyRegistrationWorkflow(Event event, List<CreateEventRegistrationWorkflowStepDto> steps) {
        if (event == null || steps == null) {
            return;
        }

        event.getRegistrationWorkflowSteps().clear();
        int index = 1;
        for (CreateEventRegistrationWorkflowStepDto stepDto : steps) {
            if (stepDto == null) {
                continue;
            }

            EventWorkflowStepType stepType = stepDto.getStepType() != null ? stepDto.getStepType() : EventWorkflowStepType.SEND_MESSAGE;
            Message message = null;
            if (stepType == EventWorkflowStepType.SEND_MESSAGE) {
                if (stepDto.getMessageId() == null) {
                    continue;
                }
                message = messageRepository.findById(stepDto.getMessageId())
                        .orElseThrow(() -> new ObjectNotFoundException(Message.class.getSimpleName(), stepDto.getMessageId()));

                if (message.getEvents() == null || message.getEvents().stream().noneMatch(e -> e.getId().equals(event.getId()))) {
                    message.getEvents().add(event);
                    messageRepository.save(message);
                }
            }

            if (stepType == EventWorkflowStepType.DELAY) {
                Integer delayMinutes = stepDto.getDelayMinutes();
                if (delayMinutes == null || delayMinutes <= 0) {
                    throw new IllegalArgumentException("Delay minutes must be greater than 0.");
                }
            }

            if (stepType == EventWorkflowStepType.CONDITION) {
                EventWorkflowConditionType conditionType = stepDto.getConditionType();
                if (conditionType == null) {
                    throw new IllegalArgumentException("Condition type is required.");
                }
            }

            if (stepType == EventWorkflowStepType.SEND_MESSAGE && message == null) {
                continue;
            }

            EventRegistrationWorkflowStep step = new EventRegistrationWorkflowStep();
            step.setEvent(event);
            step.setMessage(message);
            step.setStepType(stepType);
            step.setRecipientType(stepDto.getRecipientType() != null ? stepDto.getRecipientType() : EventWorkflowRecipientType.REGISTRANT);
            step.setDelayMinutes(stepDto.getDelayMinutes());
            step.setConditionType(stepDto.getConditionType());
            step.setConditionValue(trimToNull(stepDto.getConditionValue()));
            step.setPosition(stepDto.getPosition() != null ? stepDto.getPosition() : index);
            step.setEnabled(stepDto.getEnabled() == null || stepDto.getEnabled());
            step.setRecipientEmails(trimToNull(stepDto.getRecipientEmails()));
            event.getRegistrationWorkflowSteps().add(step);
            index++;
        }
    }

    private String normalizeTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_EVENT_ZONE.getId();
        }
        ZoneId.of(timezone.trim());
        return timezone.trim();
    }

    private ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_EVENT_ZONE;
        }
        return ZoneId.of(timezone.trim());
    }

    private EventMode resolveMode(EventMode mode) {
        return mode != null ? mode : EventMode.ONSITE;
    }

    private String generateUniqueSlug(String title, Long excludeEventId) {
        String base = slugify(title);
        String candidate = base;
        int suffix = 2;

        while (slugExists(candidate, excludeEventId)) {
            candidate = base + "-" + suffix;
            suffix++;
        }

        return candidate;
    }

    private boolean slugExists(String slug, Long excludeEventId) {
        if (excludeEventId == null) {
            return eventRepository.existsBySlug(slug);
        }
        return eventRepository.existsBySlugAndIdNot(slug, excludeEventId);
    }

    private String slugify(String value) {
        String input = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = normalized
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "event" : slug;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sanitizeRichTextToNull(String value) {
        String sanitized = eventRichTextSanitizerService.sanitizeHtml(value);
        return sanitized.isBlank() ? null : sanitized;
    }

    private String serializeHighlights(List<String> highlights) {
        if (highlights == null) {
            return null;
        }

        List<String> normalized = highlights.stream()
                .map(this::trimToNull)
                .filter(value -> value != null)
                .toList();

        if (normalized.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize highlights", e);
        }
    }

    private String serializeAgenda(List<EventAgendaItemDto> agenda) {
        if (agenda == null) {
            return null;
        }

        List<EventAgendaItemDto> normalized = agenda.stream()
                .map(this::normalizeAgendaItem)
                .filter(item -> item != null)
                .toList();

        if (normalized.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize agenda", e);
        }
    }

    private List<String> parseHighlights(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {})
                    .stream()
                    .map(this::trimToNull)
                    .filter(value -> value != null)
                    .toList();
        } catch (Exception ignored) {
            return raw.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.replaceFirst("^[-*]\\s*", ""))
                    .toList();
        }
    }

    private List<EventAgendaItemDto> parseAgenda(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        try {
            List<EventAgendaItemDto> parsed = objectMapper.readValue(raw, new TypeReference<List<EventAgendaItemDto>>() {});
            return parsed.stream()
                    .map(this::normalizeAgendaItem)
                    .filter(item -> item != null)
                    .toList();
        } catch (Exception ignored) {
            List<EventAgendaItemDto> items = new ArrayList<>();
            for (String line : raw.lines().toList()) {
                String trimmed = line.trim();
                if (trimmed.isBlank()) {
                    continue;
                }

                String[] chunks = trimmed.split("\\|", -1);
                if (chunks.length >= 4) {
                    items.add(normalizeAgendaItem(new EventAgendaItemDto(
                            chunks[0].trim(),
                            chunks[1].trim(),
                            chunks[2].trim(),
                            chunks[3].trim()
                    )));
                } else {
                    items.add(normalizeAgendaItem(new EventAgendaItemDto("", trimmed, "", "")));
                }
            }
            return items.stream().filter(item -> item != null).toList();
        }
    }

    private EventAgendaItemDto normalizeAgendaItem(EventAgendaItemDto item) {
        if (item == null) {
            return null;
        }

        String time = trimToNull(item.getTime());
        String title = trimToNull(item.getTitle());
        String speaker = trimToNull(item.getSpeaker());
        String description = sanitizeRichTextToNull(item.getDescription());

        if (time == null && title == null && speaker == null && description == null) {
            return null;
        }

        return new EventAgendaItemDto(
                time != null ? time : "",
                title != null ? title : "",
                speaker != null ? speaker : "",
                description != null ? description : ""
        );
    }

    private void ensureEventRegistrationTag(Event event) {
        if (event == null || event.getId() == null) {
            return;
        }

        String tagName = "EVENT-REG-" + event.getId();
        tagRepository.findByName(tagName).orElseGet(() -> {
            Tag tag = new Tag();
            tag.setName(tagName);
            tag.setColorCode("#2563EB");
            tag.setDescription("Contacts inscrits via le lien public de l evenement " + event.getId());
            tag.setEnabled(true);
            return tagRepository.save(tag);
        });
    }

    private void deleteEventRegistrationTags(Long eventId) {
        String prefix = EVENT_REGISTRATION_TAG_PREFIX + eventId;
        List<Tag> eventTags = tagRepository.findAllByNameStartingWith(prefix);
        if (eventTags == null || eventTags.isEmpty()) {
            return;
        }

        for (Tag tag : eventTags) {
            Tag tagWithContacts = tagRepository.findByIdWithContacts(tag.getId()).orElse(tag);
            if (tagWithContacts.getContacts() != null && !tagWithContacts.getContacts().isEmpty()) {
                List<Contact> linkedContacts = new ArrayList<>(tagWithContacts.getContacts());
                for (Contact contact : linkedContacts) {
                    if (contact.getTags() != null) {
                        contact.getTags().removeIf(contactTag -> contactTag.getId().equals(tagWithContacts.getId()));
                    }
                }
                contactRepository.saveAll(linkedContacts);
            }
            tagRepository.deleteById(tagWithContacts.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventRegistrationStatsDto getRegistrationStats(Long eventId, Instant from, Instant to) {
        String tagName = EVENT_REGISTRATION_TAG_PREFIX + eventId;
        Optional<Tag> tagOpt = tagRepository.findByNameWithContacts(tagName);

        if (tagOpt.isEmpty() || tagOpt.get().getContacts() == null || tagOpt.get().getContacts().isEmpty()) {
            return buildEmptyStats();
        }

        Set<Contact> allContacts = tagOpt.get().getContacts();
        Instant now = Instant.now();

        // --- KPIs all-time ---
        long total  = allContacts.size();
        long active = allContacts.stream().filter(Contact::isEnabled).count();

        Instant lastRegistrationAt = allContacts.stream()
                .map(Contact::getCreatedAt).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);

        // --- FenÃªtres fixes 7 j / 30 j (backward compat SSE) ---
        Instant start7  = now.minus(7,  ChronoUnit.DAYS);
        Instant start14 = now.minus(14, ChronoUnit.DAYS);
        Instant start30 = now.minus(30, ChronoUnit.DAYS);
        Instant start60 = now.minus(60, ChronoUnit.DAYS);

        long newLast7  = allContacts.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(start7)).count();
        long prev7     = allContacts.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(start14) && c.getCreatedAt().isBefore(start7)).count();
        long newLast30 = allContacts.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(start30)).count();
        long prev30    = allContacts.stream().filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(start60) && c.getCreatedAt().isBefore(start30)).count();

        double growthWeek  = computeGrowthRate(newLast7,  prev7);
        double growthMonth = computeGrowthRate(newLast30, prev30);

        // --- FenÃªtre dynamique (from / to) ---
        boolean isAllTime     = (from == null && to == null);
        Instant effectiveFrom = from != null ? from : Instant.EPOCH;
        Instant effectiveTo   = to   != null ? to   : now;

        Set<Contact> windowContacts = isAllTime ? allContacts : allContacts.stream()
                .filter(c -> c.getCreatedAt() != null
                        && !c.getCreatedAt().isBefore(effectiveFrom)
                        && !c.getCreatedAt().isAfter(effectiveTo))
                .collect(Collectors.toSet());

        long newInPeriod = isAllTime ? total : windowContacts.size();

        long previousPeriodCount = 0L;
        if (!isAllTime) {
            long windowDays = ChronoUnit.DAYS.between(effectiveFrom, effectiveTo);
            if (windowDays > 0) {
                Instant prevFrom = effectiveFrom.minus(windowDays, ChronoUnit.DAYS);
                Instant prevTo   = effectiveFrom;
                previousPeriodCount = allContacts.stream()
                        .filter(c -> c.getCreatedAt() != null
                                && !c.getCreatedAt().isBefore(prevFrom)
                                && c.getCreatedAt().isBefore(prevTo))
                        .count();
            }
        }
        double growthRatePeriod = isAllTime ? 0.0 : computeGrowthRate(newInPeriod, previousPeriodCount);

        // --- Tendance (granularitÃ© auto) ---
        DateTimeFormatter dayFmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        long rangeDays = isAllTime ? Long.MAX_VALUE : ChronoUnit.DAYS.between(effectiveFrom, effectiveTo);

        List<DailyRegistrationCountDto> dailyTrend = new ArrayList<>();

        if (rangeDays > 90) {
            // AgrÃ©gation mensuelle
            Set<Contact> trendContacts = windowContacts;
            Map<YearMonth, Long> countByMonth = trendContacts.stream()
                    .filter(c -> c.getCreatedAt() != null)
                    .collect(Collectors.groupingBy(
                            c -> YearMonth.from(c.getCreatedAt().atZone(ZoneOffset.UTC)),
                            Collectors.counting()
                    ));
            YearMonth startMonth = isAllTime
                    ? countByMonth.keySet().stream().min(Comparator.naturalOrder()).orElse(YearMonth.now())
                    : YearMonth.from(effectiveFrom.atZone(ZoneOffset.UTC));
            YearMonth endMonth = YearMonth.from(effectiveTo.atZone(ZoneOffset.UTC));
            YearMonth mCursor  = startMonth;
            while (!mCursor.isAfter(endMonth)) {
                dailyTrend.add(new DailyRegistrationCountDto(mCursor.format(monthFmt), countByMonth.getOrDefault(mCursor, 0L)));
                mCursor = mCursor.plusMonths(1);
            }
        } else {
            // AgrÃ©gation journaliÃ¨re
            Map<LocalDate, Long> countByDay = windowContacts.stream()
                    .filter(c -> c.getCreatedAt() != null)
                    .collect(Collectors.groupingBy(
                            c -> c.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                            Collectors.counting()
                    ));
            LocalDate startDay = effectiveFrom.atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate endDay   = effectiveTo.atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate dayCursor = startDay;
            while (!dayCursor.isAfter(endDay)) {
                dailyTrend.add(new DailyRegistrationCountDto(dayCursor.format(dayFmt), countByDay.getOrDefault(dayCursor, 0L)));
                dayCursor = dayCursor.plusDays(1);
            }
        }

        // --- RÃ©partitions (sur les contacts de la fenÃªtre) ---
        Map<String, Long> byCivility = buildTopMap(windowContacts.stream()
                .map(Contact::getCivility).filter(v -> v != null && !v.isBlank())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting())));

        Map<String, Long> byAgeRange = buildTopMap(windowContacts.stream()
                .map(Contact::getAgeRange).filter(v -> v != null && !v.isBlank())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting())));

        Map<String, Long> byProfession = buildTopMap(windowContacts.stream()
                .map(Contact::getProfession).filter(v -> v != null && !v.isBlank())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting())));

        Map<String, Long> byCountry = buildTopMap(windowContacts.stream()
                .filter(c -> c.getPerson() != null && c.getPerson().getCountry() != null && !c.getPerson().getCountry().isBlank())
                .collect(Collectors.groupingBy(c -> c.getPerson().getCountry(), Collectors.counting())));

        Map<String, Long> byLanguage = buildTopMap(windowContacts.stream()
                .filter(c -> c.getPerson() != null && c.getPerson().getLanguage() != null && !c.getPerson().getLanguage().isBlank())
                .collect(Collectors.groupingBy(c -> c.getPerson().getLanguage(), Collectors.counting())));

        return new EventRegistrationStatsDto(
                total, active,
                newLast7, prev7, growthWeek,
                newLast30, prev30, growthMonth,
                lastRegistrationAt, dailyTrend,
                byCivility, byAgeRange, byCountry, byLanguage, byProfession,
                newInPeriod, previousPeriodCount, growthRatePeriod
        );
    }

    private EventRegistrationStatsDto buildEmptyStats() {
        return new EventRegistrationStatsDto(
                0L, 0L,
                0L, 0L, 0.0,
                0L, 0L, 0.0,
                null, List.of(),
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                0L, 0L, 0.0
        );
    }

    private double computeGrowthRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round(((current - previous) / (double) previous) * 10000.0) / 100.0;
    }

    /**
     * Retourne un Map ordonnÃ© avec les 5 premiÃ¨res entrÃ©es + agrÃ©gation "Autres".
     */
    private Map<String, Long> buildTopMap(Map<String, Long> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        List<Map.Entry<String, Long>> sorted = raw.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        Map<String, Long> result = new LinkedHashMap<>();
        long othersCount = 0L;
        int rank = 0;
        for (Map.Entry<String, Long> entry : sorted) {
            if (rank < 5) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                othersCount += entry.getValue();
            }
            rank++;
        }
        if (othersCount > 0) {
            result.put("Autres", othersCount);
        }
        return result;
    }
}
