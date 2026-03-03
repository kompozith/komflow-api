package com.kompozith.komflow.features.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventRegistrationResponseDto {
    private String status;
    private String message;
    private String eventSlug;
    private Long contactId;
    private Long personId;
}

