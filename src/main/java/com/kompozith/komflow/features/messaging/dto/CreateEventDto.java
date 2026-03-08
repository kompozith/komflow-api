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
public class CreateEventDto {
    private String title;
    private String description;
    private String location;
    private String subtitle;
    private String address;
    private EventMode mode;
    private String meetingUrl;
    private String bannerImageUrl;
    private List<String> highlights;
    private List<EventAgendaItemDto> agenda;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private Instant startAt;
    private Instant endAt;
    private String timezone;
}
