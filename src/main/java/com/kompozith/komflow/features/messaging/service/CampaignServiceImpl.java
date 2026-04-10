package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.dto.TagWithContactCountDto;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.messaging.dto.CampaignContactResultDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDetailsDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignEditabilityDto;
import com.kompozith.komflow.features.messaging.dto.CampaignResultsSummaryDto;
import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.ScheduleCampaignDto;
import com.kompozith.komflow.features.messaging.entity.Campaign;
import com.kompozith.komflow.features.messaging.entity.CampaignContactResult;
import com.kompozith.komflow.features.messaging.entity.CampaignSendStatus;
import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import com.kompozith.komflow.features.messaging.mapper.CampaignMapper;
import com.kompozith.komflow.features.messaging.repository.CampaignContactResultRepository;
import com.kompozith.komflow.features.messaging.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {
    private static final String EVENT_REGISTRATION_TAG_PREFIX = "EVENT-REG-";

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final MessageService messageService;
    private final ContactRepository contactRepository;
    private final TagRepository tagRepository;
    private final CampaignExecutionService campaignExecutionService;
    private final CampaignContactResultRepository campaignContactResultRepository;

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

        CampaignDetailsDto detailsDto = campaignMapper.campaignToCampaignDetailsDto(campaign);
        appendEventRegistrationTagForDisplay(campaign, detailsDto);
        return detailsDto;
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignEditabilityDto getEditability(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), id));
        return evaluateEditability(campaign);
    }

    @Override
    @Transactional
    public CampaignDto update(Long id, CreateCampaignDto createCampaignDto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), id));

        CampaignEditabilityDto editability = evaluateEditability(campaign);
        if (!Boolean.TRUE.equals(editability.getEditable())) {
            throw new IllegalStateException(editability.getReason());
        }

        campaign.setName(createCampaignDto.getName());
        campaign.setDescription(createCampaignDto.getDescription());

        // Update message if provided
        if (createCampaignDto.getMessageId() != null) {
            campaign.setMessage(messageService.findEntityById(createCampaignDto.getMessageId()));
        }

        // Update contacts if provided
        if (createCampaignDto.getContactIds() != null) {
            List<Contact> contacts = contactRepository.findAllById(createCampaignDto.getContactIds());
            campaign.setContacts(contacts);
        }

        // Update tags if provided
        if (createCampaignDto.getTagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(createCampaignDto.getTagIds());
            campaign.setTags(tags);
        }

        // Update CC contacts if provided
        if (createCampaignDto.getMailCcIds() != null) {
            List<Contact> mailCc = contactRepository.findAllById(createCampaignDto.getMailCcIds());
            campaign.setMailCcContacts(mailCc);
        }

        // Update CC tags if provided
        if (createCampaignDto.getMailCcTagIds() != null) {
            List<Tag> mailCcTags = tagRepository.findAllById(createCampaignDto.getMailCcTagIds());
            campaign.setMailCcTags(mailCcTags);
        }

        // Update CCI contacts if provided
        if (createCampaignDto.getMailCciIds() != null) {
            List<Contact> mailCci = contactRepository.findAllById(createCampaignDto.getMailCciIds());
            campaign.setMailCciContacts(mailCci);
        }

        // Update CCI tags if provided
        if (createCampaignDto.getMailCciTagIds() != null) {
            List<Tag> mailCciTags = tagRepository.findAllById(createCampaignDto.getMailCciTagIds());
            campaign.setMailCciTags(mailCciTags);
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
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));

        // If campaign has a scheduled date in the future, set it to SCHEDULED status
        if (campaign.getScheduledAt() != null && campaign.getScheduledAt().isAfter(Instant.now())) {
            campaign.setStatus(CampaignStatus.SCHEDULED);
            campaignRepository.save(campaign);
            log.info("Campaign {} scheduled for {}", campaignId, campaign.getScheduledAt());
        } else {
            // Send immediately
            campaignExecutionService.startCampaign(campaignId);
        }
    }

    @Override
    @Transactional
    public CampaignDto scheduleCampaign(Long campaignId, Instant scheduledAt) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));

        // Only DRAFT or SCHEDULED campaigns can be rescheduled
        if (campaign.getStatus() != CampaignStatus.DRAFT && campaign.getStatus() != CampaignStatus.SCHEDULED) {
            throw new IllegalStateException("Only DRAFT or SCHEDULED campaigns can be rescheduled");
        }

        // Validate scheduled date is in the future
        if (scheduledAt == null || !scheduledAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Scheduled date must be in the future");
        }

        campaign.setScheduledAt(scheduledAt);
        campaign.setStatus(CampaignStatus.SCHEDULED);
        
        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign {} scheduled for {}", campaignId, scheduledAt);
        
        return campaignMapper.campaignToCampaignDto(updatedCampaign);
    }

    @Override
    @Transactional
    public CampaignDto cancelSchedule(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));

        // SECURE VALIDATION: Campaign must be in SCHEDULED status
        if (campaign.getStatus() != CampaignStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED campaigns can have their schedule cancelled. Current status: " + campaign.getStatus());
        }

        // SECURE VALIDATION: Scheduled date must be in the future
        if (campaign.getScheduledAt() == null) {
            throw new IllegalStateException("Campaign has no scheduled date to cancel");
        }

        if (!campaign.getScheduledAt().isAfter(Instant.now())) {
            throw new IllegalStateException("Cannot cancel schedule: the scheduled time has already passed. Campaign execution may be in progress.");
        }

        // SECURE VALIDATION: Double-check that campaign is not already running
        // (race condition protection)
        if (campaign.getStatus() == CampaignStatus.RUNNING) {
            throw new IllegalStateException("Cannot cancel schedule: campaign is already running");
        }

        // SECURE VALIDATION: Check that campaign has not already completed or failed
        if (campaign.getStatus() == CampaignStatus.SUCCESS || 
            campaign.getStatus() == CampaignStatus.PARTIAL_SUCCESS ||
            campaign.getStatus() == CampaignStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel schedule: campaign has already completed with status: " + campaign.getStatus());
        }

        // Reset campaign to DRAFT status and clear scheduled date
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setScheduledAt(null);
        
        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign {} schedule cancelled successfully. Returned to DRAFT status", campaignId);
        
        return campaignMapper.campaignToCampaignDto(updatedCampaign);
    }

    private CampaignEditabilityDto evaluateEditability(Campaign campaign) {
        CampaignStatus status = campaign.getStatus();
        Instant now = Instant.now();

        if (status == CampaignStatus.DRAFT) {
            return new CampaignEditabilityDto(true, status, null);
        }

        if (status == CampaignStatus.SCHEDULED) {
            if (campaign.getScheduledAt() == null || !campaign.getScheduledAt().isAfter(now)) {
                return new CampaignEditabilityDto(false, status, "Scheduled campaign is no longer editable because execution time is reached");
            }
            return new CampaignEditabilityDto(true, status, null);
        }

        return new CampaignEditabilityDto(false, status, "Only DRAFT or future SCHEDULED campaigns are editable");
    }

    private void appendEventRegistrationTagForDisplay(Campaign campaign, CampaignDetailsDto detailsDto) {
        if (campaign == null || detailsDto == null || campaign.getMessage() == null || campaign.getMessage().getFirstEvent() == null
                || campaign.getMessage().getFirstEvent().getId() == null) {
            return;
        }

        String eventTagName = EVENT_REGISTRATION_TAG_PREFIX + campaign.getMessage().getFirstEvent().getId();
        Tag eventTag = tagRepository.findByName(eventTagName).orElse(null);
        if (eventTag == null) {
            return;
        }

        List<TagWithContactCountDto> currentTags = detailsDto.getTags() == null
                ? new java.util.ArrayList<>()
                : new java.util.ArrayList<>(detailsDto.getTags());
        boolean alreadyPresent = currentTags.stream()
                .anyMatch(tag -> tag != null && tag.getId() != null && tag.getId().equals(eventTag.getId()));
        if (alreadyPresent) {
            return;
        }

        TagWithContactCountDto eventTagDto = new TagWithContactCountDto();
        eventTagDto.setId(eventTag.getId());
        eventTagDto.setName(eventTag.getName());
        eventTagDto.setDescription(eventTag.getDescription());
        eventTagDto.setColorCode(eventTag.getColorCode());
        eventTagDto.setEnabled(eventTag.isEnabled());
        eventTagDto.setCreatedAt(eventTag.getCreatedAt());
        eventTagDto.setUpdatedAt(eventTag.getUpdatedAt());
        eventTagDto.setContactCount(eventTag.getContacts() != null ? (long) eventTag.getContacts().size() : 0L);
        currentTags.add(eventTagDto);
        detailsDto.setTags(currentTags);
    }

    // ─── Send-result queries ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignContactResultDto> getCampaignResults(
            Long campaignId, CampaignSendStatus status, String search, Pageable pageable) {

        boolean hasSearch = search != null && !search.isBlank();

        Page<CampaignContactResult> page;
        if (hasSearch && status != null) {
            page = campaignContactResultRepository.findByCampaignIdAndStatusWithSearch(campaignId, status, search.trim(), pageable);
        } else if (hasSearch) {
            page = campaignContactResultRepository.findByCampaignIdWithSearch(campaignId, search.trim(), pageable);
        } else if (status != null) {
            page = campaignContactResultRepository.findByCampaignIdAndStatus(campaignId, status, pageable);
        } else {
            page = campaignContactResultRepository.findByCampaignId(campaignId, pageable);
        }

        return page.map(this::toResultDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResultsSummaryDto getCampaignResultsSummary(Long campaignId) {
        long success = campaignContactResultRepository.countByCampaignIdAndStatus(campaignId, CampaignSendStatus.SUCCESS);
        long failed  = campaignContactResultRepository.countByCampaignIdAndStatus(campaignId, CampaignSendStatus.FAILED);

        // -- Compute total unique target contacts --------------------------------
        // Start with direct contacts + tag contacts (UNION ensures deduplication)
        Set<Long> targetIds = new java.util.HashSet<>(
                campaignRepository.findDirectAndTagContactIds(campaignId));

        // Add contacts from the event-registration tag if the campaign message
        // is linked to an event
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));
        if (campaign.getMessage() != null && campaign.getMessage().getFirstEvent() != null
                && campaign.getMessage().getFirstEvent().getId() != null) {
            String eventTagName = EVENT_REGISTRATION_TAG_PREFIX + campaign.getMessage().getFirstEvent().getId();
            tagRepository.findByNameWithContacts(eventTagName).ifPresent(tag -> {
                if (tag.getContacts() != null) {
                    tag.getContacts().forEach(c -> targetIds.add(c.getId()));
                }
            });
        }

        return new CampaignResultsSummaryDto(success, failed, success + failed, targetIds.size());
    }

    @Override
    public void resubmitCampaign(Long campaignId) {
        campaignExecutionService.resubmitCampaign(campaignId);
    }

    private CampaignContactResultDto toResultDto(CampaignContactResult r) {
        var person = r.getContact() != null ? r.getContact().getPerson() : null;
        return new CampaignContactResultDto(
                r.getId(),
                r.getContact() != null ? r.getContact().getId() : null,
                person != null ? person.getEmail() : null,
                person != null ? person.getFirstName() : null,
                person != null ? person.getLastName() : null,
                r.getChannel() != null ? r.getChannel().name() : null,
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getCreatedAt(),
                r.getErrorMessage()
        );
    }
}
