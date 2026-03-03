package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.EventMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String location;
    private String subtitle;
    private String address;
    private EventMode mode;
    private String meetingUrl;
    private List<String> highlights;
    private List<EventAgendaItemDto> agenda;
    private LocalDate eventDate;
    private Instant startAt;
    private Instant endAt;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;
}
