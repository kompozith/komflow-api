package com.kompozith.komflow.features.contact.controller;

import com.kompozith.komflow.features.contact.dto.PublicEventDetailsDto;
import com.kompozith.komflow.features.contact.dto.PublicEventAgendaItemDto;
import com.kompozith.komflow.features.contact.dto.PublicEventRequestMetadataDto;
import com.kompozith.komflow.features.contact.dto.PublicEventRegistrationRequestDto;
import com.kompozith.komflow.features.contact.dto.PublicEventRegistrationResponseDto;
import com.kompozith.komflow.features.contact.dto.PublicEventScheduleDto;
import com.kompozith.komflow.features.contact.service.ContactService;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/events")
@Tag(name = "Public Events", description = "Public APIs for event details and guest registration")
public class PublicEventController {

    private final ContactService contactService;
    private final EventService eventService;

    @GetMapping("/{slug}")
    @Operation(summary = "Get public event details", description = "Returns event information for guest visitors")
    public PublicEventDetailsDto getEvent(@PathVariable String slug, HttpServletRequest httpRequest) {
        EventDto event = eventService.findBySlug(slug);
        PublicEventRequestMetadataDto metadata = buildRequestMetadata(httpRequest);
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
                event.getBannerImageUrl(),
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
                        : List.of(),
                buildSchedule(event, metadata)
        );
    }

    @PostMapping("/{slug}/register")
    @Operation(summary = "Register to public event", description = "Creates or updates participant information without conflict errors")
    public PublicEventRegistrationResponseDto register(
            @PathVariable String slug,
            @Valid @RequestBody PublicEventRegistrationRequestDto request,
            HttpServletRequest httpRequest
    ) {
        EventDto event = eventService.findBySlug(slug);
        return contactService.registerPublicEvent(event.getId(), event.getSlug(), request, buildRequestMetadata(httpRequest));
    }

    private PublicEventRequestMetadataDto buildRequestMetadata(HttpServletRequest request) {
        if (request == null) {
            return new PublicEventRequestMetadataDto();
        }

        String language = firstNonBlank(
                request.getHeader("X-Language"),
                request.getHeader("Accept-Language"),
                request.getLocale() != null ? request.getLocale().toLanguageTag() : null
        );
        String timezone = firstNonBlank(
                request.getHeader("X-Timezone"),
                request.getHeader("Time-Zone"),
                request.getHeader("CF-Timezone")
        );
        String country = firstNonBlank(
                request.getHeader("X-Country-Code"),
                request.getHeader("CF-IPCountry"),
                request.getHeader("CloudFront-Viewer-Country"),
                request.getHeader("X-AppEngine-Country"),
                request.getHeader("X-Vercel-IP-Country")
        );
        String city = firstNonBlank(
                request.getHeader("X-City"),
                request.getHeader("CF-IPCity"),
                request.getHeader("X-AppEngine-City"),
                request.getHeader("X-Vercel-IP-City")
        );
        String userAgent = request.getHeader("User-Agent");
        String clientIp = resolveClientIp(request);

        return new PublicEventRequestMetadataDto(language, timezone, country, city, clientIp, userAgent);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr() == null ? null : request.getRemoteAddr().trim();
    }

    private PublicEventScheduleDto buildSchedule(EventDto event, PublicEventRequestMetadataDto metadata) {
        if (event == null || event.getStartAt() == null) {
            return null;
        }

        ZoneId zoneId = resolveSafeZoneId(metadata == null ? null : metadata.getTimezone(), event.getTimezone());
        Locale locale = resolveSafeLocale(metadata == null ? null : metadata.getLanguage());
        String timezoneLabel = "Heure locale (" + zoneId.getId() + ")";

        Instant startsAt = event.getStartAt();
        Instant endsAt = event.getEndAt();
        if (endsAt == null) {
            return new PublicEventScheduleDto(
                    timezoneLabel,
                    false,
                    formatDateTime(startsAt, zoneId, locale),
                    null,
                    null,
                    null,
                    null
            );
        }

        boolean sameDayRange = isSameLocalDay(startsAt, endsAt, zoneId);
        if (sameDayRange) {
            return new PublicEventScheduleDto(
                    timezoneLabel,
                    true,
                    null,
                    formatDate(startsAt, zoneId, locale),
                    formatTime(startsAt, zoneId, locale) + " - " + formatTime(endsAt, zoneId, locale) + " (" + formatDuration(startsAt, endsAt) + ")",
                    null,
                    null
            );
        }

        return new PublicEventScheduleDto(
                timezoneLabel,
                false,
                null,
                null,
                null,
                formatDateTime(startsAt, zoneId, locale),
                formatDateTime(endsAt, zoneId, locale)
        );
    }

    private ZoneId resolveSafeZoneId(String requestedTimezone, String eventTimezone) {
        String candidate = firstNonBlank(requestedTimezone, eventTimezone, "UTC");
        try {
            return ZoneId.of(candidate);
        } catch (Exception ignored) {
            return ZoneId.of("UTC");
        }
    }

    private Locale resolveSafeLocale(String languageHeader) {
        String firstTag = extractPrimaryLanguageTag(languageHeader);
        if (firstTag == null) {
            return Locale.FRENCH;
        }
        Locale locale = Locale.forLanguageTag(firstTag);
        if (locale.getLanguage() == null || locale.getLanguage().isBlank()) {
            return Locale.FRENCH;
        }
        return locale;
    }

    private String extractPrimaryLanguageTag(String languageHeader) {
        if (languageHeader == null || languageHeader.isBlank()) {
            return null;
        }
        String first = languageHeader.split(",")[0];
        String withoutQuality = first.split(";")[0].trim();
        return withoutQuality.isBlank() ? null : withoutQuality;
    }

    private String formatDate(Instant instant, ZoneId zoneId, Locale locale) {
        return DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", locale)
                .withZone(zoneId)
                .format(instant);
    }

    private String formatTime(Instant instant, ZoneId zoneId, Locale locale) {
        return DateTimeFormatter.ofPattern("HH:mm", locale)
                .withZone(zoneId)
                .format(instant);
    }

    private String formatDateTime(Instant instant, ZoneId zoneId, Locale locale) {
        return DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy, HH:mm", locale)
                .withZone(zoneId)
                .format(instant);
    }

    private boolean isSameLocalDay(Instant start, Instant end, ZoneId zoneId) {
        String startDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).withZone(zoneId).format(start);
        String endDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).withZone(zoneId).format(end);
        return startDate.equals(endDate);
    }

    private String formatDuration(Instant start, Instant end) {
        long minutes = Math.max(0, Duration.between(start, end).toMinutes());
        long hours = minutes / 60;
        long remainder = minutes % 60;

        if (hours > 0 && remainder > 0) {
            return hours + "h" + String.format("%02d", remainder);
        }
        if (hours > 0) {
            return hours + "h";
        }
        return minutes + "min";
    }
}
