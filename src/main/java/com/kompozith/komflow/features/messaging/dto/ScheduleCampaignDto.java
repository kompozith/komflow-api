package com.kompozith.komflow.features.messaging.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for scheduling a campaign to be sent at a specific date and time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCampaignDto {

    @NotNull(message = "Scheduled date/time is required")
    @Future(message = "Scheduled date/time must be in the future")
    private Instant scheduledAt;
}
