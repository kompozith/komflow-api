package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.dto.CampaignContactResultDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDetailsDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignEditabilityDto;
import com.kompozith.komflow.features.messaging.dto.CampaignResultsSummaryDto;
import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.ScheduleCampaignDto;
import com.kompozith.komflow.features.messaging.entity.CampaignSendStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface CampaignService {
    CampaignDto create(CreateCampaignDto createCampaignDto);
    List<CampaignDto> findAll();
    Page<CampaignDto> findAll(Pageable pageable);
    CampaignDetailsDto findById(Long id);
    CampaignEditabilityDto getEditability(Long id);
    CampaignDto update(Long id, CreateCampaignDto createCampaignDto);
    void delete(Long id);
    void sendCampaign(Long campaignId);
    CampaignDto scheduleCampaign(Long campaignId, Instant scheduledAt);
    CampaignDto cancelSchedule(Long campaignId);

    /**
     * Returns the paginated send results for a campaign.
     * Pass {@code null} for {@code status} to retrieve all results.
     * Pass {@code null} or empty for {@code search} to skip text filtering.
     */
    Page<CampaignContactResultDto> getCampaignResults(Long campaignId, CampaignSendStatus status, String search, Pageable pageable);

    /** Returns success/failed/total counts for a campaign. */
    CampaignResultsSummaryDto getCampaignResultsSummary(Long campaignId);

    /**
     * Resubmits a FAILED or PARTIAL_SUCCESS campaign, retrying only contacts
     * whose previous send attempt failed.
     */
    void resubmitCampaign(Long campaignId);
}
