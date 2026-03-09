package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.messaging.dto.CampaignEventDto;
import com.kompozith.komflow.features.messaging.dto.CampaignEventType;
import com.kompozith.komflow.features.messaging.entity.Campaign;
import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignExecutionService {
    private static final String EVENT_REGISTRATION_TAG_PREFIX = "EVENT-REG-";

    private final CampaignRepository campaignRepository;
    private final TagRepository tagRepository;
    private final MessageDispatcherService messageDispatcherService;
    private final CampaignEventStreamService campaignEventStreamService;
    @Qualifier("campaignExecutor")
    private final Executor campaignExecutor;
    private final PlatformTransactionManager transactionManager;

    public void startCampaign(Long campaignId) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));

            if (campaign.getStatus() == CampaignStatus.RUNNING) {
                throw new IllegalStateException("Campaign is already running");
            }

            if (!hasAtLeastOneRecipientSource(campaign)) {
                throw new IllegalStateException("Campaign has no contacts or tags to send to");
            }

            campaign.setStatus(CampaignStatus.RUNNING);
            campaignRepository.save(campaign);
        });

        campaignExecutor.execute(() -> runCampaign(campaignId));
    }

    private void runCampaign(Long campaignId) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        ExecutionData executionData = template.execute(status -> loadExecutionData(campaignId));
        if (executionData == null) {
            return;
        }

        int total = executionData.contacts.size();
        int successCount = 0;
        int failureCount = 0;
        int processed = 0;

        campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                .type(CampaignEventType.STARTED)
                .campaignId(campaignId)
                .timestamp(Instant.now())
                .total(total)
                .processed(0)
                .successCount(0)
                .failureCount(0)
                .build());

        for (Contact contact : executionData.contacts) {
            processed++;
            campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                    .type(CampaignEventType.IN_PROGRESS)
                    .campaignId(campaignId)
                    .timestamp(Instant.now())
                    .total(total)
                    .processed(processed)
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .contactId(contact.getId())
                    .build());

            try {
                if (messageDispatcherService.canSendToContact(contact, executionData.message.getChannel())) {
                    messageDispatcherService.sendToContact(contact, executionData.message, executionData.message.getChannel(), executionData.eventInstantUtc);
                    successCount++;
                    campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                            .type(CampaignEventType.SUCCESS)
                            .campaignId(campaignId)
                            .timestamp(Instant.now())
                            .total(total)
                            .processed(processed)
                            .successCount(successCount)
                            .failureCount(failureCount)
                            .contactId(contact.getId())
                            .recipient(messageDispatcherService.getRecipientIdentifier(contact, executionData.message.getChannel()))
                            .build());
                } else {
                    failureCount++;
                    campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                            .type(CampaignEventType.FAILED)
                            .campaignId(campaignId)
                            .timestamp(Instant.now())
                            .total(total)
                            .processed(processed)
                            .successCount(successCount)
                            .failureCount(failureCount)
                            .contactId(contact.getId())
                            .message("Contact has no valid recipient for channel " + executionData.message.getChannel())
                            .build());
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to send message to contact {} in campaign {}: {}", contact.getId(), campaignId, e.getMessage());
                campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                        .type(CampaignEventType.FAILED)
                        .campaignId(campaignId)
                        .timestamp(Instant.now())
                        .total(total)
                        .processed(processed)
                        .successCount(successCount)
                        .failureCount(failureCount)
                        .contactId(contact.getId())
                        .message(e.getMessage())
                        .build());
            }
        }

        CampaignStatus finalStatus;
        if (failureCount == 0) {
            finalStatus = CampaignStatus.SUCCESS;
        } else if (successCount == 0) {
            finalStatus = CampaignStatus.FAILED;
        } else {
            finalStatus = CampaignStatus.PARTIAL_SUCCESS;
        }

        template.executeWithoutResult(status -> {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));
            campaign.setStatus(finalStatus);
            campaignRepository.save(campaign);
        });

        campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                .type(CampaignEventType.COMPLETED)
                .campaignId(campaignId)
                .timestamp(Instant.now())
                .total(total)
                .processed(processed)
                .successCount(successCount)
                .failureCount(failureCount)
                .status(finalStatus)
                .build());
    }

    private ExecutionData loadExecutionData(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));

        Message message = campaign.getMessage();
        if (message != null) {
            message.getChannel();
            message.getContent();
            if (message.getAttachments() != null) {
                message.getAttachments().size();
            }
        }

        Set<Contact> uniqueContacts = new HashSet<>();
        if (campaign.getContacts() != null) {
            uniqueContacts.addAll(campaign.getContacts());
        }
        if (campaign.getTags() != null) {
            for (Tag tag : campaign.getTags()) {
                if (tag.getContacts() != null) {
                    uniqueContacts.addAll(tag.getContacts());
                }
            }
        }
        if (message != null && message.getEvent() != null && message.getEvent().getId() != null) {
            String eventTagName = EVENT_REGISTRATION_TAG_PREFIX + message.getEvent().getId();
            tagRepository.findByNameWithContacts(eventTagName).ifPresent(eventTag -> {
                if (eventTag.getContacts() != null) {
                    uniqueContacts.addAll(eventTag.getContacts());
                }
            });
        }

        for (Contact contact : uniqueContacts) {
            if (contact.getPerson() != null) {
                contact.getPerson().getEmail();
                if (contact.getPerson().getPhoneNumbers() != null) {
                    contact.getPerson().getPhoneNumbers().size();
                }
            }
        }

        // Event-local variables must reflect the linked event date/time, not campaign scheduling time.
        Instant eventInstantUtc = null;
        if (message != null && message.getEvent() != null && message.getEvent().getStartAt() != null) {
            eventInstantUtc = message.getEvent().getStartAt();
        } else if (campaign.getScheduledAt() != null) {
            eventInstantUtc = campaign.getScheduledAt();
        } else {
            eventInstantUtc = Instant.now();
        }
        return new ExecutionData(message, uniqueContacts, eventInstantUtc);
    }

    private boolean hasAtLeastOneRecipientSource(Campaign campaign) {
        boolean hasDirectContacts = campaign.getContacts() != null && !campaign.getContacts().isEmpty();
        boolean hasDirectTags = campaign.getTags() != null && !campaign.getTags().isEmpty();
        if (hasDirectContacts || hasDirectTags) {
            return true;
        }
        if (campaign.getMessage() == null || campaign.getMessage().getEvent() == null || campaign.getMessage().getEvent().getId() == null) {
            return false;
        }
        String eventTagName = EVENT_REGISTRATION_TAG_PREFIX + campaign.getMessage().getEvent().getId();
        return tagRepository.findByName(eventTagName).isPresent();
    }

    private static class ExecutionData {
        private final Message message;
        private final Set<Contact> contacts;
        private final Instant eventInstantUtc;

        private ExecutionData(Message message, Set<Contact> contacts, Instant eventInstantUtc) {
            this.message = message;
            this.contacts = contacts;
            this.eventInstantUtc = eventInstantUtc;
        }
    }
}
