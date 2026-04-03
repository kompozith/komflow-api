package com.kompozith.komflow.features.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResultsSummaryDto {
    private long successCount;
    private long failedCount;
    /** successCount + failedCount — contacts that were actually attempted. */
    private long totalCount;
    /**
     * Total unique contacts the campaign intended to reach (direct contacts +
     * tag members + event-registration tag members). May be greater than
     * {@code totalCount} if execution was interrupted before all contacts were
     * attempted.
     */
    private long totalTargetCount;
}

