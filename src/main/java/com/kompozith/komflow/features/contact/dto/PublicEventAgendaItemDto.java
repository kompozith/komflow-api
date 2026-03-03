package com.kompozith.komflow.features.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventAgendaItemDto {
    private String time;
    private String title;
    private String speaker;
    private String description;
}

