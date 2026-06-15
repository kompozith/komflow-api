package com.kompozith.komflow.features.billing.service;

import com.kompozith.komflow.features.billing.port.BillingProviderPort;
import com.kompozith.komflow.features.billing.port.BillingProviderPort.BillingEvent;
import com.kompozith.komflow.features.billing.port.BillingProviderPort.CheckoutCommand;
import com.kompozith.komflow.features.billing.port.BillingProviderPort.CheckoutResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Orchestrateur billing — Operator Stack Pattern.
 *
 * Sélectionne l'adaptateur actif via {@code app.billing.provider-type}.
 * Tous les beans {@link BillingProviderPort} sont auto-découverts par Spring ;
 * ajouter un provider revient à créer un {@code @Service} sans toucher ici.
 *
 * <pre>
 * app:
 *   billing:
 *     provider-type: STUB     # STUB | STRIPE | PADDLE | LEMONSQUEEZY | …
 * </pre>
 */
@Service
@Slf4j
public class BillingOperatorStack {

  /** Code du provider actif, défini en configuration. */
  @Value("${app.billing.provider-type:STUB}")
  private String activeProviderType;

  private final List<BillingProviderPort> providers;
  private BillingProviderPort activeProvider;

  public BillingOperatorStack(List<BillingProviderPort> providers) {
    this.providers = providers;
  }

  @PostConstruct
  void init() {
    activeProvider = providers.stream()
        .filter(p -> p.providerCode().equalsIgnoreCase(activeProviderType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No BillingProviderPort registered for provider-type '%s'. Available: %s"
            .formatted(activeProviderType, providers.stream().map(BillingProviderPort::providerCode).toList())));

    log.info("[BillingOperatorStack] Active billing provider: {}", activeProvider.providerCode());
  }

  // ── Façade publique ───────────────────────────────────────────────────────

  public CheckoutResult createCheckoutSession(CheckoutCommand cmd) {
    log.debug("[BillingStack] createCheckout org={} plan={} provider={}",
        cmd.organizationId(), cmd.planCode(), activeProvider.providerCode());
    return activeProvider.createCheckoutSession(cmd);
  }

  public String createPortalSession(Long orgId, String returnUrl) {
    log.debug("[BillingStack] createPortal org={} provider={}", orgId, activeProvider.providerCode());
    return activeProvider.createPortalSession(orgId, returnUrl);
  }

  public BillingEvent verifyAndParseWebhook(byte[] payload, Map<String, String> headers) {
    return activeProvider.verifyAndParseWebhook(payload, headers);
  }

  /** Retourne le code du provider actuellement actif (pour monitoring/debug). */
  public String activeProviderCode() {
    return activeProvider.providerCode();
  }
}
