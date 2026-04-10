package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.dto.CreateEventDto;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.dto.EventRegistrationStatsDto;
import com.kompozith.komflow.features.messaging.entity.Event;

import java.time.Instant;
import java.util.List;

public interface EventService {
    EventDto create(CreateEventDto createEventDto);
    List<EventDto> findAll();
    List<EventDto> findFuture(Instant from);
    List<EventDto> findCalendarRange(Instant start, Instant end);
    EventDto findById(Long id);
    EventDto findBySlug(String slug);
    EventDto update(Long id, CreateEventDto createEventDto);
    void delete(Long id);
    Event findEntityById(Long id);
    EventRegistrationStatsDto getRegistrationStats(Long eventId, Instant from, Instant to);
}
