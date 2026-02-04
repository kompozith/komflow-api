package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CampaignEventDto {
    private final CampaignEventType type;
    private final Long campaignId;
    private final Instant timestamp;
    private final Integer total;
    private final Integer processed;
    private final Integer successCount;
    private final Integer failureCount;
    private final Long contactId;
    private final String recipient;
    private final String message;
    private final CampaignStatus status;
}
