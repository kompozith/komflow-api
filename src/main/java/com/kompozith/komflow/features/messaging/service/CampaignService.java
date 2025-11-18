package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CampaignService {
    CampaignDto create(CreateCampaignDto createCampaignDto);
    List<CampaignDto> findAll();
    Page<CampaignDto> findAll(Pageable pageable);
    CampaignDto findById(Long id);
    CampaignDto update(Long id, CreateCampaignDto createCampaignDto);
    void delete(Long id);
    void sendCampaign(Long campaignId);
}