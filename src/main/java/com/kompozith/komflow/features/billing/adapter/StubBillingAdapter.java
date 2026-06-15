package com.kompozith.komflow.features.billing.adapter;

import com.kompozith.komflow.features.billing.port.BillingProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptateur billing STUB — aucun appel réseau.
 * Actif par défaut ({@code app.billing.provider-type=STUB}).
 *
 * Permet de développer et tester toute la logique de plan/quota
 * sans dépendre d'un provider de paiement réel.
 * Les URLs retournées sont fictives mais traçables dans les logs.
 */
@Service
@Slf4j
public class StubBillingAdapter implements BillingProviderPort {

  @Override
  public String providerCode() {
    return "STUB";
  }

  @Override
  public CheckoutResult createCheckoutSession(CheckoutCommand cmd) {
    String sessionId = "stub_session_" + UUID.randomUUID().toString().substring(0, 8);
    String url = cmd.successUrl() + "?stub_session=" + sessionId;
    log.info("[StubBilling] CHECKOUT org={} plan={} trial={}d → sessionId={} url={}",
        cmd.organizationId(), cmd.planCode(), cmd.trialDays(), sessionId, url);
    return new CheckoutResult(url, sessionId);
  }

  @Override
  public String createPortalSession(Long orgId, String returnUrl) {
    log.info("[StubBilling] PORTAL org={} returnUrl={}", orgId, returnUrl);
    return returnUrl + "?stub_portal=1";
  }

  @Override
  public BillingEvent verifyAndParseWebhook(byte[] payload, Map<String, String> headers) {
    // En mode STUB, on accepte tout et on simule un paiement réussi
    log.info("[StubBilling] WEBHOOK received ({} bytes) — simulating PAYMENT_SUCCEEDED", payload.length);
    return new BillingEvent(
        BillingEventType.PAYMENT_SUCCEEDED,
        null,   // organizationId inconnu sans vraie charge
        "FREE",
        Instant.now().plus(30, ChronoUnit.DAYS),
        0L,
        "stub_evt_" + UUID.randomUUID().toString().substring(0, 8)
    );
  }
}
