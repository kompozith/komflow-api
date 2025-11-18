package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Message ID is required")
    private Long messageId;

    private List<Long> contactIds;
    private List<Long> mailCcIds;
    private List<Long> mailCciIds;

    private CampaignStatus status = CampaignStatus.READY;
    private Instant scheduledAt;
}