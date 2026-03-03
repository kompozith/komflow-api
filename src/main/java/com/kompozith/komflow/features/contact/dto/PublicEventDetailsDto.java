package com.kompozith.komflow.features.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.kompozith.komflow.features.messaging.entity.EventMode;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventDetailsDto {
    private String slug;
    private String title;
    private String subtitle;
    private String description;
    private EventMode mode;
    private Instant startsAt;
    private Instant endsAt;
    private String location;
    private String address;
    private String meetingUrl;
    private List<String> highlights;
    private List<PublicEventAgendaItemDto> agenda;
}
