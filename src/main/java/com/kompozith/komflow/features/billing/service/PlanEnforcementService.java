package com.kompozith.komflow.features.billing.service;

import com.kompozith.komflow.features.billing.entity.Plan;
import com.kompozith.komflow.features.billing.entity.UsageCounter;
import com.kompozith.komflow.features.billing.exception.PlanLimitExceededException;
import com.kompozith.komflow.features.billing.repository.PlanRepository;
import com.kompozith.komflow.features.billing.repository.SubscriptionRepository;
import com.kompozith.komflow.features.billing.repository.UsageCounterRepository;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'enforcement des quotas SaaS.
 * Ne dépend d'aucun opérateur externe (Redis-free par design — DB uniquement).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanEnforcementService {

  private final PlanRepository         planRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final UsageCounterRepository usageCounterRepository;

  // ── Assertions (levées avant exécution) ─────────────────────────────────────

  /**
   * Vérifie que l'organisation peut soumettre une campagne supplémentaire.
   * @throws PlanLimitExceededException HTTP 402 si quota dépassé.
   */
  @Transactional(readOnly = true)
  public void assertCanSubmitCampaign(Long organizationId) {
    Plan plan = resolvePlan(organizationId);
    if (plan.getMaxCampaignsPerMonth() < 0) return; // illimité

    long current = getCurrentCount(organizationId, "CAMPAIGNS");
    if (current >= plan.getMaxCampaignsPerMonth()) {
      throw new PlanLimitExceededException("CAMPAIGNS", current, plan.getMaxCampaignsPerMonth());
    }
  }

  /**
   * Vérifie que l'organisation peut envoyer sur le canal indiqué.
   * @throws PlanLimitExceededException HTTP 402 si quota dépassé ou canal non autorisé.
   */
  @Transactional(readOnly = true)
  public void assertCanSendChannel(Long organizationId, MessageChannel channel) {
    Plan plan = resolvePlan(organizationId);

    switch (channel) {
      case EMAIL -> {
        if (plan.getMaxEmailsPerMonth() == 0) throw deny("EMAIL", 0, 0);
        long current = getCurrentCount(organizationId, "EMAIL");
        if (plan.getMaxEmailsPerMonth() > 0 && current >= plan.getMaxEmailsPerMonth()) {
          throw new PlanLimitExceededException("EMAIL", current, plan.getMaxEmailsPerMonth());
        }
      }
      case SMS -> {
        if (!plan.isCanUseSms()) throw deny("SMS", 0, 0);
        long current = getCurrentCount(organizationId, "SMS");
        if (plan.getMaxSmsPerMonth() > 0 && current >= plan.getMaxSmsPerMonth()) {
          throw new PlanLimitExceededException("SMS", current, plan.getMaxSmsPerMonth());
        }
      }
      case WHATSAPP -> {
        if (!plan.isCanUseWhatsapp()) throw deny("WHATSAPP", 0, 0);
        long current = getCurrentCount(organizationId, "WHATSAPP");
        if (plan.getMaxWhatsappPerMonth() > 0 && current >= plan.getMaxWhatsappPerMonth()) {
          throw new PlanLimitExceededException("WHATSAPP", current, plan.getMaxWhatsappPerMonth());
        }
      }
    }
  }

  // ── Comptage ────────────────────────────────────────────────────────────────

  /**
   * Incrémente un compteur d'usage (upsert DB).
   */
  @Transactional
  public void recordUsage(Long organizationId, String metric, long amount) {
    String yearMonth = UsageCounter.currentYearMonth();
    int updated = usageCounterRepository.incrementCounter(organizationId, metric, yearMonth, amount);
    if (updated == 0) {
      // Première occurrence du mois → création
      UsageCounter counter = UsageCounter.builder()
          .organizationId(organizationId)
          .metric(metric)
          .yearMonth(yearMonth)
          .count(amount)
          .build();
      usageCounterRepository.save(counter);
    }
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private Plan resolvePlan(Long organizationId) {
    String planCode = subscriptionRepository
        .findByOrganizationId(organizationId)
        .map(sub -> sub.getPlanCode())
        .orElse("FREE");
    return planRepository.findById(planCode).orElseGet(() -> buildFreePlan());
  }

  private long getCurrentCount(Long organizationId, String metric) {
    return usageCounterRepository
        .findByOrganizationIdAndMetricAndYearMonth(
            organizationId, metric, UsageCounter.currentYearMonth())
        .map(UsageCounter::getCount)
        .orElse(0L);
  }

  private PlanLimitExceededException deny(String metric, long used, long limit) {
    return new PlanLimitExceededException(metric, used, limit);
  }

  private Plan buildFreePlan() {
    return Plan.builder()
        .code("FREE")
        .label("Free")
        .maxContactsTotal(500)
        .maxCampaignsPerMonth(2)
        .maxEmailsPerMonth(1000)
        .maxSmsPerMonth(0)
        .maxWhatsappPerMonth(0)
        .canUseSms(false)
        .canUseWhatsapp(false)
        .build();
  }
}
