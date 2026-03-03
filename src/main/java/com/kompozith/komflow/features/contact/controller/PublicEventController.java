package com.kompozith.komflow.features.contact.controller;

import com.kompozith.komflow.features.contact.dto.PublicEventDetailsDto;
import com.kompozith.komflow.features.contact.dto.PublicEventAgendaItemDto;
import com.kompozith.komflow.features.contact.dto.PublicEventRegistrationRequestDto;
import com.kompozith.komflow.features.contact.dto.PublicEventRegistrationResponseDto;
import com.kompozith.komflow.features.contact.service.ContactService;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/events")
@Tag(name = "Public Events", description = "Public APIs for event details and guest registration")
public class PublicEventController {

    private final ContactService contactService;
    private final EventService eventService;

    @GetMapping("/{slug}")
    @Operation(summary = "Get public event details", description = "Returns event information for guest visitors")
    public PublicEventDetailsDto getEvent(@PathVariable String slug) {
        EventDto event = eventService.findBySlug(slug);
        return new PublicEventDetailsDto(
                event.getSlug(),
                event.getTitle(),
                event.getSubtitle(),
                event.getDescription(),
                event.getMode(),
                event.getStartAt(),
                event.getEndAt(),
                event.getLocation(),
                event.getAddress(),
                event.getMeetingUrl(),
                event.getHighlights() != null ? event.getHighlights() : List.of(),
                event.getAgenda() != null
                        ? event.getAgenda().stream()
                        .map(item -> new PublicEventAgendaItemDto(
                                item.getTime(),
                                item.getTitle(),
                                item.getSpeaker(),
                                item.getDescription()
                        ))
                        .toList()
                        : List.of()
        );
    }

    @PostMapping("/{slug}/register")
    @Operation(summary = "Register to public event", description = "Creates or updates participant information without conflict errors")
    public PublicEventRegistrationResponseDto register(
            @PathVariable String slug,
            @Valid @RequestBody PublicEventRegistrationRequestDto request
    ) {
        EventDto event = eventService.findBySlug(slug);
        return contactService.registerPublicEvent(event.getSlug(), request);
    }
}
