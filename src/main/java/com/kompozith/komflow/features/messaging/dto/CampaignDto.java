package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {
    private Long id;
    private String name;
    private String description;
    private MessageDto message;
    private List<Long> contactIds;
    private List<Long> mailCcIds;
    private List<Long> mailCciIds;
    private CampaignStatus status;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant updatedAt;
}
