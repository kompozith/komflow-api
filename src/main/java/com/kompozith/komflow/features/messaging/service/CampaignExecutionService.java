package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.messaging.dto.CampaignEventDto;
import com.kompozith.komflow.features.messaging.dto.CampaignEventType;
import com.kompozith.komflow.features.messaging.entity.Campaign;
import com.kompozith.komflow.features.messaging.entity.CampaignContactResult;
import com.kompozith.komflow.features.messaging.entity.CampaignSendStatus;
import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.repository.CampaignContactResultRepository;
import com.kompozith.komflow.features.messaging.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignExecutionService {
    private static final String EVENT_REGISTRATION_TAG_PREFIX = "EVENT-REG-";

    private final CampaignRepository campaignRepository;
    private final TagRepository tagRepository;
    private final ContactRepository contactRepository;
    private final CampaignContactResultRepository campaignContactResultRepository;
    private final MessageDispatcherService messageDispatcherService;
    private final CampaignEventStreamService campaignEventStreamService;
    /** Sends the HTML recap e-mail to admins when a campaign finishes. */
    private final CampaignCompletionNotificationService completionNotificationService;
    @Qualifier("campaignExecutor")
    private final Executor campaignExecutor;
    private final PlatformTransactionManager transactionManager;

    /**
     * Minimum delay (ms) between two consecutive email sends.
     * Keeps the send rate well below Gmail's per-minute threshold.
     * Default: 500 ms (~120 emails/min). Raise it if auth failures persist.
     */
    @Value("${app.campaign.email-throttle-ms:500}")
    private long emailThrottleMs;

    /**
     * Number of consecutive AUTH failures that trigger an automatic pause.
     * After this many back-to-back failures the campaign pauses for
     * {@code authFailurePauseMs} to let Gmail lift its temporary block.
     */
    @Value("${app.campaign.max-consecutive-auth-failures:5}")
    private int maxConsecutiveAuthFailures;

    /**
     * How long (ms) to pause when the auth-failure circuit-breaker trips.
     * Default: 120 000 ms (2 minutes).
     */
    @Value("${app.campaign.auth-failure-pause-ms:120000}")
    private long authFailurePauseMs;

    /**
     * A campaign that has been in RUNNING status for longer than this many
     * milliseconds without any new result row being persisted is considered
     * orphaned (execution thread crashed / JVM restarted). The watcher job
     * will resolve it to the appropriate final status.
     * Default: 3 hours.
     */
    @Value("${app.campaign.stale-running-threshold-ms:10800000}")
    private long staleRunningThresholdMs;

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

        campaignExecutor.execute(() -> runCampaign(campaignId, Collections.emptySet()));
    }

    /**
     * Resubmits a campaign that previously ended with FAILED or PARTIAL_SUCCESS.
     * Only contacts whose send result was FAILED (or who were never reached) are
     * processed; contacts that already received the message successfully are skipped.
     */
    public void resubmitCampaign(Long campaignId) {
        Set<Long> successContactIds = new HashSet<>();
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new ObjectNotFoundException(Campaign.class.getSimpleName(), campaignId));

            if (campaign.getStatus() != CampaignStatus.FAILED
                    && campaign.getStatus() != CampaignStatus.PARTIAL_SUCCESS) {
                throw new IllegalStateException(
                        "Only FAILED or PARTIAL_SUCCESS campaigns can be resubmitted. Current status: "
                                + campaign.getStatus());
            }

            // Collect contact IDs that were successfully reached in previous runs
            successContactIds.addAll(
                    campaignContactResultRepository.findContactIdsByCampaignIdAndStatus(
                            campaignId, CampaignSendStatus.SUCCESS));

            campaign.setStatus(CampaignStatus.RUNNING);
            campaignRepository.save(campaign);
        });

        campaignExecutor.execute(() -> runCampaign(campaignId, successContactIds));
    }

    private void runCampaign(Long campaignId, Set<Long> skipContactIds) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        ExecutionData executionData = template.execute(status -> loadExecutionData(campaignId));
        if (executionData == null) {
            return;
        }

        // Filter out contacts that were already successfully reached
        if (!skipContactIds.isEmpty()) {
            executionData.contacts.removeIf(c -> c.getId() != null && skipContactIds.contains(c.getId()));
            log.info("Campaign {} resubmit: skipping {} already-successful contacts, {} remaining",
                    campaignId, skipContactIds.size(), executionData.contacts.size());
        }

        // Record wall-clock start so we can report duration in the admin recap
        Instant startedAt = Instant.now();

        int total = executionData.contacts.size();
        int successCount = 0;
        int failureCount = 0;
        int processed = 0;
        int consecutiveAuthFailures = 0;

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

            // Throttle: pause between sends to stay within Gmail rate limits
            if (processed > 0 && emailThrottleMs > 0) {
                try {
                    Thread.sleep(emailThrottleMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Campaign {} execution interrupted during throttle sleep", campaignId);
                    break;
                }
            }

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
                    messageDispatcherService.sendToContact(contact, executionData.message,
                            executionData.message.getChannel(), executionData.eventInstantUtc);
                    successCount++;
                    consecutiveAuthFailures = 0;
                    persistResult(template, campaignId, contact,
                            executionData.message.getChannel(), CampaignSendStatus.SUCCESS, null);
                    campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                            .type(CampaignEventType.SUCCESS)
                            .campaignId(campaignId)
                            .timestamp(Instant.now())
                            .total(total)
                            .processed(processed)
                            .successCount(successCount)
                            .failureCount(failureCount)
                            .contactId(contact.getId())
                            .recipient(messageDispatcherService.getRecipientIdentifier(
                                    contact, executionData.message.getChannel()))
                            .build());
                } else {
                    failureCount++;
                    String noRecipientMsg = "Contact has no valid recipient for channel "
                            + executionData.message.getChannel();
                    persistResult(template, campaignId, contact,
                            executionData.message.getChannel(), CampaignSendStatus.FAILED, noRecipientMsg);
                    campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                            .type(CampaignEventType.FAILED)
                            .campaignId(campaignId)
                            .timestamp(Instant.now())
                            .total(total)
                            .processed(processed)
                            .successCount(successCount)
                            .failureCount(failureCount)
                            .contactId(contact.getId())
                            .message(noRecipientMsg)
                            .build());
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to send message to contact {} in campaign {}: {}",
                        contact.getId(), campaignId, e.getMessage());

                persistResult(template, campaignId, contact,
                        executionData.message.getChannel(), CampaignSendStatus.FAILED, e.getMessage());

                // Circuit-breaker: detect sustained AUTH failures
                if (isAuthenticationFailure(e)) {
                    consecutiveAuthFailures++;
                    log.warn("Consecutive auth failures: {}/{} for campaign {}",
                            consecutiveAuthFailures, maxConsecutiveAuthFailures, campaignId);

                    if (consecutiveAuthFailures >= maxConsecutiveAuthFailures) {
                        log.warn("Circuit-breaker tripped for campaign {} after {} consecutive auth failures. "
                                        + "Pausing {}ms to allow Gmail rate-limit to reset...",
                                campaignId, consecutiveAuthFailures, authFailurePauseMs);
                        try {
                            Thread.sleep(authFailurePauseMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.warn("Campaign {} execution interrupted during auth-failure pause", campaignId);
                            break;
                        }
                        consecutiveAuthFailures = 0;
                        log.info("Campaign {} resuming after auth-failure pause", campaignId);
                    }
                } else {
                    consecutiveAuthFailures = 0;
                }

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

        Instant completedAt = Instant.now();

        campaignEventStreamService.emit(campaignId, CampaignEventDto.builder()
                .type(CampaignEventType.COMPLETED)
                .campaignId(campaignId)
                .timestamp(completedAt)
                .total(total)
                .processed(processed)
                .successCount(successCount)
                .failureCount(failureCount)
                .status(finalStatus)
                .build());

        // ── Admin recap e-mail ────────────────────────────────────────────────
        // Already executing on a campaign-executor background thread, so the
        // original HTTP request that triggered startCampaign() is never blocked.
        log.info("Campaign {} finished – total={} success={} failed={} status={}. Sending admin recap…",
                campaignId, total, successCount, failureCount, finalStatus);
        completionNotificationService.notifyCampaignCompleted(
                campaignId,
                executionData.campaignName,
                total,
                successCount,
                failureCount,
                finalStatus,
                startedAt,
                completedAt
        );
    }

    /**
     * Upserts a {@link CampaignContactResult} row in its own transaction:
     * <ul>
     *   <li>If a row already exists for the (campaign, contact) pair it is
     *       updated in-place (status + errorMessage). This handles resubmissions
     *       correctly — a previously FAILED row becomes SUCCESS without creating
     *       a duplicate.</li>
     *   <li>If no row exists a new one is inserted.</li>
     * </ul>
     * Errors are swallowed and logged so that a persistence failure never blocks
     * the send loop.
     */
    private void persistResult(TransactionTemplate template,
                                Long campaignId, Contact contact,
                                MessageChannel channel, CampaignSendStatus status,
                                String errorMessage) {
        try {
            template.executeWithoutResult(tx -> {
                CampaignContactResult result =
                        campaignContactResultRepository
                                .findByCampaignIdAndContactId(campaignId, contact.getId())
                                .orElseGet(() -> {
                                    CampaignContactResult r = new CampaignContactResult();
                                    r.setCampaign(campaignRepository.getReferenceById(campaignId));
                                    r.setContact(contactRepository.getReferenceById(contact.getId()));
                                    r.setChannel(channel);
                                    return r;
                                });
                result.setStatus(status);
                result.setErrorMessage(errorMessage);
                campaignContactResultRepository.save(result);
            });
        } catch (Exception ex) {
            log.warn("Could not persist send result for campaign {} contact {}: {}",
                    campaignId, contact.getId(), ex.getMessage());
        }
    }

    /**
     * Returns true when the exception (or any cause in its chain) indicates
     * that the SMTP server rejected the authentication credentials.
     */
    private boolean isAuthenticationFailure(Exception e) {
        if (e instanceof MailAuthenticationException) {
            return true;
        }
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof MailAuthenticationException) {
                return true;
            }
            String msg = cause.getMessage();
            if (msg != null && (msg.contains("Authentication failed")
                    || msg.contains("AUTH LOGIN failed")
                    || msg.contains("535"))) {
                return true;
            }
            cause = cause.getCause();
        }
        String msg = e.getMessage();
        return msg != null && (msg.contains("Authentication failed")
                || msg.contains("AUTH LOGIN failed")
                || msg.contains("535"));
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

        // TreeSet with a deterministic comparator so the send order is always
        // reproducible from SQL: ORDER BY c.created_at ASC, p.first_name ASC
        Comparator<Contact> contactOrder = Comparator
                .comparing(
                        (Contact c) -> c.getCreatedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .thenComparing(
                        c -> c.getPerson() != null ? c.getPerson().getFirstName() : null,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .thenComparingLong(c -> c.getId() != null ? c.getId() : Long.MAX_VALUE); // tie-breaker

        Set<Contact> uniqueContacts = new TreeSet<>(contactOrder);
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
        if (message != null && message.getFirstEvent() != null && message.getFirstEvent().getId() != null) {
            String eventTagName = EVENT_REGISTRATION_TAG_PREFIX + message.getFirstEvent().getId();
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

        Instant eventInstantUtc = null;
        if (message != null && message.getFirstEvent() != null && message.getFirstEvent().getStartAt() != null) {
            eventInstantUtc = message.getFirstEvent().getStartAt();
        } else if (campaign.getScheduledAt() != null) {
            eventInstantUtc = campaign.getScheduledAt();
        } else {
            eventInstantUtc = Instant.now();
        }
        return new ExecutionData(message, uniqueContacts, eventInstantUtc, campaign.getName());
    }

    private boolean hasAtLeastOneRecipientSource(Campaign campaign) {
        boolean hasDirectContacts = campaign.getContacts() != null && !campaign.getContacts().isEmpty();
        boolean hasDirectTags = campaign.getTags() != null && !campaign.getTags().isEmpty();
        if (hasDirectContacts || hasDirectTags) {
            return true;
        }
        if (campaign.getMessage() == null || campaign.getMessage().getFirstEvent() == null
                || campaign.getMessage().getFirstEvent().getId() == null) {
            return false;
        }
        String eventTagName = EVENT_REGISTRATION_TAG_PREFIX + campaign.getMessage().getFirstEvent().getId();
        return tagRepository.findByName(eventTagName).isPresent();
    }

    private static class ExecutionData {
        private final Message message;
        private final Set<Contact> contacts;
        private final Instant eventInstantUtc;
        /** Display name of the campaign – used in the admin completion recap. */
        private final String campaignName;

        private ExecutionData(Message message, Set<Contact> contacts, Instant eventInstantUtc, String campaignName) {
            this.message = message;
            this.contacts = contacts;
            this.eventInstantUtc = eventInstantUtc;
            this.campaignName = campaignName;
        }
    }

    // ─── Stale-RUNNING watcher ────────────────────────────────────────────────

    /**
     * Called by the scheduler to detect campaigns stuck in RUNNING status whose
     * execution thread is no longer active.
     *
     * <p>Two cases are handled:
     * <ol>
     *   <li><b>All contacts processed</b>: the result count equals the computed
     *       target count – the execution finished but crashed before persisting
     *       the final status update.</li>
     *   <li><b>Orphaned execution</b>: the last result row is older than
     *       {@code staleRunningThresholdMs} (or no result rows exist and the
     *       campaign has been RUNNING for that long) – the execution thread was
     *       killed mid-run.</li>
     * </ol>
     */
    public void resolveStaleRunning() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        List<Campaign> runningCampaigns = tx.execute(
                s -> campaignRepository.findByStatus(CampaignStatus.RUNNING));
        if (runningCampaigns == null || runningCampaigns.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (Campaign campaign : runningCampaigns) {
            try {
                resolveIfStale(campaign.getId(), now);
            } catch (Exception e) {
                log.error("Error while resolving stale campaign {}: {}", campaign.getId(), e.getMessage(), e);
            }
        }
    }

    private void resolveIfStale(Long campaignId, Instant now) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(s -> {
            Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
            if (campaign == null || campaign.getStatus() != CampaignStatus.RUNNING) {
                return; // already resolved by another thread
            }

            long processedCount = campaignContactResultRepository.countByCampaignId(campaignId);
            long targetCount    = countTargetContacts(campaign);

            boolean allProcessed = targetCount > 0 && processedCount >= targetCount;

            Instant lastActivity = campaignContactResultRepository.findLastResultCreatedAt(campaignId);
            // Fall back to the campaign's own updatedAt (= when it was set to RUNNING)
            Instant lastSeen = lastActivity != null ? lastActivity : campaign.getUpdatedAt();
            boolean isIdle = lastSeen == null
                    || lastSeen.isBefore(now.minus(staleRunningThresholdMs, ChronoUnit.MILLIS));

            if (!allProcessed && !isIdle) {
                return; // still running normally
            }

            long successCount = campaignContactResultRepository.countByCampaignIdAndStatus(campaignId, CampaignSendStatus.SUCCESS);
            long failedCount  = campaignContactResultRepository.countByCampaignIdAndStatus(campaignId, CampaignSendStatus.FAILED);

            CampaignStatus finalStatus;
            if (failedCount == 0 && successCount > 0) {
                finalStatus = CampaignStatus.SUCCESS;
            } else if (successCount == 0) {
                finalStatus = CampaignStatus.FAILED;
            } else {
                finalStatus = CampaignStatus.PARTIAL_SUCCESS;
            }

            campaign.setStatus(finalStatus);
            campaignRepository.save(campaign);
            log.warn("Stale RUNNING campaign {} resolved to {} "
                            + "(processed={} target={} success={} failed={} allProcessed={} isIdle={})",
                    campaignId, finalStatus, processedCount, targetCount,
                    successCount, failedCount, allProcessed, isIdle);
        });
    }

    /** Counts the distinct contacts the campaign is supposed to reach. */
    private long countTargetContacts(Campaign campaign) {
        Set<Long> ids = new HashSet<>(campaignRepository.findDirectAndTagContactIds(campaign.getId()));
        if (campaign.getMessage() != null && campaign.getMessage().getFirstEvent() != null
                && campaign.getMessage().getFirstEvent().getId() != null) {
            String eventTagName = EVENT_REGISTRATION_TAG_PREFIX + campaign.getMessage().getFirstEvent().getId();
            tagRepository.findByNameWithContacts(eventTagName).ifPresent(tag -> {
                if (tag.getContacts() != null) {
                    tag.getContacts().forEach(c -> ids.add(c.getId()));
                }
            });
        }
        return ids.size();
    }
}
