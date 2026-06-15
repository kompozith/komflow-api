package com.kompozith.komflow.features.billing.service;

import com.kompozith.komflow.features.billing.entity.Subscription;
import com.kompozith.komflow.features.billing.port.BillingProviderPort.BillingEvent;
import com.kompozith.komflow.features.billing.port.BillingProviderPort.BillingEventType;
import com.kompozith.komflow.features.billing.repository.SubscriptionRepository;
import com.kompozith.komflow.features.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Traite les événements billing normalisés et met à jour la Subscription en base.
 * Totalement découplé du provider : reçoit uniquement des {@link BillingEvent}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingWebhookHandler {

  private final SubscriptionRepository subscriptionRepository;
  private final OrganizationRepository organizationRepository;

  @Transactional
  public void handle(BillingEvent event) {
    log.info("[BillingWebhook] event={} org={} plan={} extId={}",
        event.type(), event.organizationId(), event.planCode(), event.externalEventId());

    if (event.organizationId() == null) {
      log.warn("[BillingWebhook] organizationId absent — événement ignoré: {}", event.externalEventId());
      return;
    }

    BillingEventType type = event.type();

    switch (type) {
      case SUBSCRIPTION_ACTIVATED, SUBSCRIPTION_RENEWED, PAYMENT_SUCCEEDED -> activate(event);
      case SUBSCRIPTION_CANCELED                                             -> cancel(event);
      case SUBSCRIPTION_PAST_DUE                                             -> pastDue(event);
      case TRIAL_WILL_END -> log.info("[BillingWebhook] Trial ending soon for org={}", event.organizationId());
      default             -> log.debug("[BillingWebhook] Unhandled event type: {}", type);
    }
  }

  // ── Handlers internes ────────────────────────────────────────────────────

  private void activate(BillingEvent event) {
    Subscription sub = findOrCreate(event.organizationId());
    sub.setPlanCode(event.planCode() != null ? event.planCode() : sub.getPlanCode());
    sub.setStatus("ACTIVE");
    sub.setCurrentPeriodEnd(event.periodEnd());
    sub.setExternalId(event.externalEventId());
    subscriptionRepository.save(sub);
    log.info("[BillingWebhook] Subscription ACTIVATED org={} plan={}", event.organizationId(), sub.getPlanCode());
  }

  private void cancel(BillingEvent event) {
    subscriptionRepository.findByOrganizationId(event.organizationId()).ifPresent(sub -> {
      sub.setStatus("CANCELED");
      subscriptionRepository.save(sub);
      log.info("[BillingWebhook] Subscription CANCELED org={}", event.organizationId());
    });
  }

  private void pastDue(BillingEvent event) {
    subscriptionRepository.findByOrganizationId(event.organizationId()).ifPresent(sub -> {
      sub.setStatus("PAST_DUE");
      subscriptionRepository.save(sub);
      log.warn("[BillingWebhook] Subscription PAST_DUE org={}", event.organizationId());
    });
  }

  private Subscription findOrCreate(Long orgId) {
    return subscriptionRepository.findByOrganizationId(orgId)
        .orElseGet(() -> {
          Subscription s = new Subscription();
          s.setOrganization(organizationRepository.getReferenceById(orgId));
          s.setPlanCode("FREE");
          s.setStatus("ACTIVE");
          return s;
        });
  }
}
