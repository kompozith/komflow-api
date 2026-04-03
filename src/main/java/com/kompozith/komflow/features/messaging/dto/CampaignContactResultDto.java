package com.kompozith.komflow.features.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactResultDto {
    private Long id;
    private Long contactId;
    private String contactEmail;
    private String contactFirstName;
    private String contactLastName;
    /** EMAIL, SMS or WHATSAPP */
    private String channel;
    /** SUCCESS or FAILED */
    private String status;
    /** Timestamp of the send attempt (= createdAt from BaseEntity) */
    private Instant sentAt;
    /** Null when status = SUCCESS, populated with the root error otherwise */
    private String errorMessage;
}

