package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.messaging.dto.CreateEventDto;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.entity.Event;
import com.kompozith.komflow.features.messaging.mapper.EventMapper;
import com.kompozith.komflow.features.messaging.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final ZoneId DEFAULT_EVENT_ZONE = ZoneId.of("GMT");

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventDto create(CreateEventDto createEventDto) {
        validatePayload(createEventDto);
        Event event = eventMapper.createEventDtoToEvent(createEventDto);
        event.setAllDay(createEventDto.getAllDay() != null ? createEventDto.getAllDay() : false);
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
    @Transactional
    public EventDto update(Long id, CreateEventDto createEventDto) {
        validatePayload(createEventDto);
        Event event = findEntityById(id);

        event.setTitle(createEventDto.getTitle());
        event.setDescription(createEventDto.getDescription());
        event.setLocation(createEventDto.getLocation());
        event.setAllDay(createEventDto.getAllDay() != null ? createEventDto.getAllDay() : false);
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

        String timezone = normalizeTimezone(dto.getTimezone());
        Instant startAt = resolveStartAt(dto, timezone);
        if (startAt == null) {
            throw new IllegalArgumentException("Event start date is required");
        }

        Instant endAt = resolveEndAt(dto, timezone);
        if (endAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("Event end date must be greater than or equal to start date");
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
        if (dto.getStartDate() == null) {
            return null;
        }

        LocalTime localTime = dto.getStartTime() != null ? dto.getStartTime() : LocalTime.MIDNIGHT;
        return ZonedDateTime.of(dto.getStartDate(), localTime, resolveZoneId(timezone)).toInstant();
    }

    private Instant resolveEndAt(CreateEventDto dto, String timezone) {
        if (dto.getEndAt() != null) {
            return dto.getEndAt();
        }
        if (dto.getEndDate() == null) {
            return null;
        }

        LocalTime localTime = dto.getEndTime();
        if (localTime == null) {
            localTime = Boolean.TRUE.equals(dto.getAllDay()) ? LocalTime.of(23, 59) : LocalTime.MIDNIGHT;
        }

        return ZonedDateTime.of(dto.getEndDate(), localTime, resolveZoneId(timezone)).toInstant();
    }

    private EventDto toEventDto(Event event) {
        EventDto dto = eventMapper.eventToEventDto(event);
        if (dto == null || event == null) {
            return dto;
        }

        ZoneId zoneId = resolveZoneId(event.getTimezone());
        if (event.getStartAt() != null) {
            ZonedDateTime start = event.getStartAt().atZone(zoneId);
            dto.setStartDate(start.toLocalDate());
            dto.setStartTime(start.toLocalTime().withSecond(0).withNano(0));
        }
        if (event.getEndAt() != null) {
            ZonedDateTime end = event.getEndAt().atZone(zoneId);
            dto.setEndDate(end.toLocalDate());
            dto.setEndTime(end.toLocalTime().withSecond(0).withNano(0));
        }

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
}
