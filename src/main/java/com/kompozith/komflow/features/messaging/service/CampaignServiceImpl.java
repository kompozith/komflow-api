package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.entity.Campaign;
import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import com.kompozith.komflow.features.messaging.mapper.CampaignMapper;
import com.kompozith.komflow.features.messaging.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final MessageService messageService;
    private final ContactRepository contactRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public CampaignDto create(CreateCampaignDto createCampaignDto) {
        Campaign campaign = campaignMapper.createCampaignDtoToCampaign(createCampaignDto);

        // Set message
        campaign.setMessage(messageService.findEntityById(createCampaignDto.getMessageId()));

        // Set contacts
        if (createCampaignDto.getContactIds() != null && !createCampaignDto.getContactIds().isEmpty()) {
            List<Contact> contacts = contactRepository.findAllById(createCampaignDto.getContactIds());
            campaign.setContacts(contacts);
        }

        // Set CC contacts
        if (createCampaignDto.getMailCcIds() != null && !createCampaignDto.getMailCcIds().isEmpty()) {
            List<Contact> mailCc = contactRepository.findAllById(createCampaignDto.getMailCcIds());
            campaign.setMailCc(mailCc);
        }

        // Set CCI contacts
        if (createCampaignDto.getMailCciIds() != null && !createCampaignDto.getMailCciIds().isEmpty()) {
            List<Contact> mailCci = contactRepository.findAllById(createCampaignDto.getMailCciIds());
            campaign.setMailCci(mailCci);
        }

        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("Campaign created with id: {}", savedCampaign.getId());
        return campaignMapper.campaignToCampaignDto(savedCampaign);
    }

    @Override
    public List<CampaignDto> findAll() {
        return campaignRepository.findAll().stream()
                .map(campaignMapper::campaignToCampaignDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CampaignDto> findAll(Pageable pageable) {
        return campaignRepository.findAll(pageable)
                .map(campaignMapper::campaignToCampaignDto);
    }

    @Override
    public CampaignDto findById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), id));
        return campaignMapper.campaignToCampaignDto(campaign);
    }

    @Override
    @Transactional
    public CampaignDto update(Long id, CreateCampaignDto createCampaignDto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), id));

        campaign.setName(createCampaignDto.getName());
        campaign.setDescription(createCampaignDto.getDescription());
        campaign.setStatus(createCampaignDto.getStatus());
        campaign.setScheduledAt(createCampaignDto.getScheduledAt());

        // Update message if provided
        if (createCampaignDto.getMessageId() != null) {
            campaign.setMessage(messageService.findEntityById(createCampaignDto.getMessageId()));
        }

        // Update contacts if provided
        if (createCampaignDto.getContactIds() != null) {
            List<Contact> contacts = contactRepository.findAllById(createCampaignDto.getContactIds());
            campaign.setContacts(contacts);
        }

        // Update CC contacts if provided
        if (createCampaignDto.getMailCcIds() != null) {
            List<Contact> mailCc = contactRepository.findAllById(createCampaignDto.getMailCcIds());
            campaign.setMailCc(mailCc);
        }

        // Update CCI contacts if provided
        if (createCampaignDto.getMailCciIds() != null) {
            List<Contact> mailCci = contactRepository.findAllById(createCampaignDto.getMailCciIds());
            campaign.setMailCci(mailCci);
        }

        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign updated with id: {}", id);
        return campaignMapper.campaignToCampaignDto(updatedCampaign);
    }

    @Override
    public void delete(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new ObjectNotFoundException(Campaign.class.getSimpleName(), id);
        }
        campaignRepository.deleteById(id);
        log.info("Campaign deleted with id: {}", id);
    }

    @Override
    @Transactional
    public void sendCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));

        if (campaign.getContacts() == null || campaign.getContacts().isEmpty()) {
            throw new IllegalStateException("Campaign has no contacts to send to");
        }

        int successCount = 0;
        int failureCount = 0;

        for (Contact contact : campaign.getContacts()) {
            try {
                emailService.sendEmail(contact, campaign.getMessage());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send email to contact {} in campaign {}: {}", contact.getId(), campaignId, e.getMessage());
                failureCount++;
            }
        }

        // Update campaign status
        if (failureCount == 0) {
            campaign.setStatus(CampaignStatus.SENT);
        } else if (successCount == 0) {
            campaign.setStatus(CampaignStatus.FAILED);
        } else {
            campaign.setStatus(CampaignStatus.SENT); // Partial success still marked as sent
        }

        campaignRepository.save(campaign);
        log.info("Campaign {} sent: {} success, {} failures", campaignId, successCount, failureCount);
    }
}