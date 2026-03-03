package com.kompozith.komflow.features.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAgendaItemDto {
    private String time;
    private String title;
    private String speaker;
    private String description;
}
