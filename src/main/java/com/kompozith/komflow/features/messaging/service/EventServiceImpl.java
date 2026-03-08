package com.kompozith.komflow.features.messaging.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.messaging.dto.CreateEventDto;
import com.kompozith.komflow.features.messaging.dto.EventAgendaItemDto;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.entity.Event;
import com.kompozith.komflow.features.messaging.entity.EventMode;
import com.kompozith.komflow.features.messaging.mapper.EventMapper;
import com.kompozith.komflow.features.messaging.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final ZoneId DEFAULT_EVENT_ZONE = ZoneId.of("GMT");

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final EventRichTextSanitizerService eventRichTextSanitizerService;

    @Override
    @Transactional
    public EventDto create(CreateEventDto createEventDto) {
        validatePayload(createEventDto);
        Event event = eventMapper.createEventDtoToEvent(createEventDto);
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
        return toEventDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> findAll() {
        return eventRepository.findAll().stream()
                .map(this::toEventDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> findFuture(Instant from) {
        Instant start = from != null ? from : Instant.now();
        return eventRepository.findAllByStartAtGreaterThanEqualOrderByStartAtAsc(start).stream()
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

        Event saved = eventRepository.save(event);
        return toEventDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ObjectNotFoundException(Event.class.getSimpleName(), id);
        }
        eventRepository.deleteById(id);
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
        return dto;
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
}
