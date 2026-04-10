package com.kompozith.komflow.features.messaging.controller;

import com.kompozith.komflow.features.core.service.SseEmitterRegistry;
import com.kompozith.komflow.features.messaging.dto.CreateEventDto;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.dto.EventRegistrationStatsDto;
import com.kompozith.komflow.features.messaging.service.EventService;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for managing events used by calendar and messages")
public class EventController {

    private final EventService eventService;
    private final SseEmitterRegistry sseEmitterRegistry;

    @PreAuthorize("hasAuthority('MESSAGE_CREATE')")
    @PostMapping
    @Operation(summary = "Create event")
    public ResponseEntity<EventDto> create(@RequestBody CreateEventDto createEventDto) {
        return ResponseEntity.ok(eventService.create(createEventDto));
    }

    @PreAuthorize("hasAuthority('MESSAGE_LIST')")
    @GetMapping
    @Operation(summary = "List events")
    public ResponseEntity<List<EventDto>> findAll(
            @RequestParam(required = false) Instant rangeStart,
            @RequestParam(required = false) Instant rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            return ResponseEntity.ok(eventService.findCalendarRange(rangeStart, rangeEnd));
        }
        return ResponseEntity.ok(eventService.findAll());
    }

    @PreAuthorize("hasAuthority('MESSAGE_LIST')")
    @GetMapping("/future")
    @Operation(summary = "List future events")
    public ResponseEntity<List<EventDto>> findFuture(@RequestParam(required = false) Instant from) {
        return ResponseEntity.ok(eventService.findFuture(from));
    }

    @PreAuthorize("hasAuthority('MESSAGE_SHOW')")
    @GetMapping("/{id}")
    @Operation(summary = "Get event by id")
    public ResponseEntity<EventDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @PreAuthorize("hasAuthority('MESSAGE_UPDATE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update event")
    public ResponseEntity<EventDto> update(@PathVariable Long id, @RequestBody CreateEventDto createEventDto) {
        return ResponseEntity.ok(eventService.update(id, createEventDto));
    }

    @PreAuthorize("hasAuthority('MESSAGE_DELETE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event")
    public ResponseEntity<SimpleResponse> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.ok(new SimpleResponse<>("Event deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('MESSAGE_SHOW')")
    @GetMapping("/{id}/registration-stats")
    @Operation(summary = "Get event registration statistics")
    public ResponseEntity<EventRegistrationStatsDto> getRegistrationStats(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromInstant = from != null ? Instant.parse(from) : null;
        Instant toInstant   = to   != null ? Instant.parse(to)   : null;
        return ResponseEntity.ok(eventService.getRegistrationStats(id, fromInstant, toInstant));
    }

    @PreAuthorize("hasAuthority('MESSAGE_SHOW')")
    @GetMapping(value = "/{id}/registration-stats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream event registration statistics via SSE")
    public SseEmitter streamRegistrationStats(@PathVariable Long id) {
        return sseEmitterRegistry.register(id);
    }
}
