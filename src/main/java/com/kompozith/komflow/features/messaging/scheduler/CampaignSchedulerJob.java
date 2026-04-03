package com.kompozith.komflow.features.messaging.scheduler;

import com.kompozith.komflow.features.messaging.entity.Campaign;
import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import com.kompozith.komflow.features.messaging.repository.CampaignRepository;
import com.kompozith.komflow.features.messaging.service.CampaignExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled job that checks for campaigns that need to be sent based on their scheduled date/time.
 * Runs every minute to check for due campaigns.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignSchedulerJob {

    private final CampaignRepository campaignRepository;
    private final CampaignExecutionService campaignExecutionService;

    /**
     * Check every minute for scheduled campaigns that are due to be sent.
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void processScheduledCampaigns() {
        log.debug("Checking for scheduled campaigns due for execution...");
        
        Instant now = Instant.now();
        List<Campaign> dueCampaigns = campaignRepository.findScheduledCampaignsDue(CampaignStatus.SCHEDULED, now);
        
        if (dueCampaigns.isEmpty()) {
            log.debug("No scheduled campaigns due for execution");
            return;
        }
        
        log.info("Found {} scheduled campaign(s) due for execution", dueCampaigns.size());
        
        for (Campaign campaign : dueCampaigns) {
            try {
                log.info("Executing scheduled campaign {} - {}", campaign.getId(), campaign.getName());
                campaignExecutionService.startCampaign(campaign.getId());
            } catch (Exception e) {
                log.error("Failed to execute scheduled campaign {}: {}", campaign.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Check every 5 minutes for RUNNING campaigns that have stalled and need their status resolved.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void resolveStaleRunningCampaigns() {
        log.debug("Checking for stale RUNNING campaigns...");
        campaignExecutionService.resolveStaleRunning();
    }
}
