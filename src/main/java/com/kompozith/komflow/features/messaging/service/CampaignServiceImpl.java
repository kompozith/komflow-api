package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDetailsDto;
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
    private final TagRepository tagRepository;
    private final CampaignExecutionService campaignExecutionService;

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

        // Set tags
        if (createCampaignDto.getTagIds() != null && !createCampaignDto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(createCampaignDto.getTagIds());
            campaign.setTags(tags);
        }

        // Set CC contacts
        if (createCampaignDto.getMailCcIds() != null && !createCampaignDto.getMailCcIds().isEmpty()) {
            List<Contact> mailCc = contactRepository.findAllById(createCampaignDto.getMailCcIds());
            campaign.setMailCcContacts(mailCc);
        }

        // Set CC tags
        if (createCampaignDto.getMailCcTagIds() != null && !createCampaignDto.getMailCcTagIds().isEmpty()) {
            List<Tag> mailCcTags = tagRepository.findAllById(createCampaignDto.getMailCcTagIds());
            campaign.setMailCcTags(mailCcTags);
        }

        // Set CCI contacts
        if (createCampaignDto.getMailCciIds() != null && !createCampaignDto.getMailCciIds().isEmpty()) {
            List<Contact> mailCci = contactRepository.findAllById(createCampaignDto.getMailCciIds());
            campaign.setMailCciContacts(mailCci);
        }

        // Set CCI tags
        if (createCampaignDto.getMailCciTagIds() != null && !createCampaignDto.getMailCciTagIds().isEmpty()) {
            List<Tag> mailCciTags = tagRepository.findAllById(createCampaignDto.getMailCciTagIds());
            campaign.setMailCciTags(mailCciTags);
        }

        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("Campaign created with id: {}", savedCampaign.getId());
        return campaignMapper.campaignToCampaignDto(savedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignDto> findAll() {
        return campaignRepository.findAll().stream()
                .map(campaignMapper::campaignToCampaignDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignDto> findAll(Pageable pageable) {
        return campaignRepository.findAll(pageable)
                .map(campaignMapper::campaignToCampaignDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDetailsDto findById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), id));

        return campaignMapper.campaignToCampaignDetailsDto(campaign);
    }

    @Override
    @Transactional
    public CampaignDto update(Long id, CreateCampaignDto createCampaignDto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), id));

        // Prevent deletion if campaign is running
        if (campaign.getStatus() == CampaignStatus.RUNNING) {
            throw new IllegalStateException("Cannot update a campaign that is currently running");
        }

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
            campaign.setMailCcContacts(mailCc);
        }

        // Update CCI contacts if provided
        if (createCampaignDto.getMailCciIds() != null) {
            List<Contact> mailCci = contactRepository.findAllById(createCampaignDto.getMailCciIds());
            campaign.setMailCciContacts(mailCci);
        }

        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign updated with id: {}", id);
        return campaignMapper.campaignToCampaignDto(updatedCampaign);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), id));

        // Prevent deletion if campaign is running
        if (campaign.getStatus() == CampaignStatus.RUNNING) {
            throw new IllegalStateException("Cannot delete a campaign that is currently running");
        }

        campaignRepository.deleteById(id);
        log.info("Campaign deleted with id: {}", id);
    }

    @Override
    @Transactional
    public void sendCampaign(Long campaignId) {
        campaignExecutionService.startCampaign(campaignId);
    }
}
