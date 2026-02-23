package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDetailsDto;
import com.kompozith.komflow.features.messaging.dto.CampaignEditabilityDto;
import com.kompozith.komflow.features.messaging.dto.ScheduleCampaignDto;
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
}
