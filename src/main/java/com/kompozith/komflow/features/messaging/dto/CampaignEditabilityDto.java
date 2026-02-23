package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEditabilityDto {
    private Boolean editable;
    private CampaignStatus status;
    private String reason;
}

