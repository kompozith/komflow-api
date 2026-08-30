package com.kompozith.komflow.features.billing.controller;

import com.kompozith.komflow.features.billing.dto.BillingOverviewResponse;
import com.kompozith.komflow.features.billing.entity.Plan;
import com.kompozith.komflow.features.billing.entity.Subscription;
import com.kompozith.komflow.features.billing.entity.UsageCounter;
import com.kompozith.komflow.features.billing.port.BillingProviderPort;
import com.kompozith.komflow.features.billing.port.BillingProviderPort.CheckoutCommand;
import com.kompozith.komflow.features.billing.port.BillingProviderPort.BillingEvent;
import com.kompozith.komflow.features.billing.repository.PlanRepository;
import com.kompozith.komflow.features.billing.repository.SubscriptionRepository;
import com.kompozith.komflow.features.billing.repository.UsageCounterRepository;
import com.kompozith.komflow.features.billing.service.BillingOperatorStack;
import com.kompozith.komflow.features.billing.service.BillingWebhookHandler;
import com.kompozith.komflow.features.organization.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Plan SaaS, quotas et paiements")
public class BillingController {

  private final PlanRepository         planRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final UsageCounterRepository usageCounterRepository;
  private final BillingOperatorStack   billingStack;
  private final BillingWebhookHandler  webhookHandler;

  // ── Vue d'ensemble ────────────────────────────────────────────────────────

  @GetMapping("/overview")
  @Operation(summary = "Résumé du plan et quotas de l'organisation courante")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<BillingOverviewResponse> overview() {
    Long orgId = TenantContext.getOrganizationId();

    Subscription sub = subscriptionRepository.findByOrganizationId(orgId).orElse(null);
    String planCode = sub != null ? sub.getPlanCode() : "FREE";
    Plan plan = planRepository.findById(planCode).orElseGet(this::defaultFreePlan);

    String yearMonth = UsageCounter.currentYearMonth();
    Map<String, Long> counters = usageCounterRepository
        .findByOrganizationIdAndYearMonth(orgId, yearMonth)
        .stream()
        .collect(Collectors.toMap(UsageCounter::getMetric, UsageCounter::getCount));

    List<BillingOverviewResponse.QuotaUsage> quotas = List.of(
        toQuota("CAMPAIGNS",  counters, plan.getMaxCampaignsPerMonth()),
        toQuota("EMAIL",      counters, plan.getMaxEmailsPerMonth()),
        toQuota("SMS",        counters, plan.getMaxSmsPerMonth()),
        toQuota("WHATSAPP",   counters, plan.getMaxWhatsappPerMonth())
    );

    return ResponseEntity.ok(new BillingOverviewResponse(
        plan.getCode(),
        plan.getLabel(),
        sub != null ? sub.getStatus() : "NONE",
        plan.getPriceMonthlyCtsCents(),
        quotas
    ));
  }

  // ── Checkout ──────────────────────────────────────────────────────────────

  record CheckoutRequest(String planCode, String successUrl, String cancelUrl, int trialDays) {}
  record CheckoutResponse(String checkoutUrl, String sessionId) {}

  @PostMapping("/checkout")
  @Operation(summary = "Créer une session de paiement pour changer de plan")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<CheckoutResponse> checkout(
      @RequestBody CheckoutRequest req,
      @AuthenticationPrincipal UserDetails user) {

    Long orgId = TenantContext.getOrganizationId();
    // UserDetails#getUsername() returns the Spring Security principal, which is the
    // user's email since email is the sole account identifier (no more username).
    String email = user != null ? user.getUsername() : "";

    BillingProviderPort.CheckoutResult result = billingStack.createCheckoutSession(
        new CheckoutCommand(orgId, req.planCode(), email, req.successUrl(), req.cancelUrl(), req.trialDays()));

    return ResponseEntity.ok(new CheckoutResponse(result.checkoutUrl(), result.externalSessionId()));
  }

  // ── Portail client ────────────────────────────────────────────────────────

  record PortalResponse(String portalUrl) {}

  @PostMapping("/portal")
  @Operation(summary = "Créer une session portail pour gérer l'abonnement")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PortalResponse> portal(@RequestParam String returnUrl) {
    Long orgId = TenantContext.getOrganizationId();
    String url = billingStack.createPortalSession(orgId, returnUrl);
    return ResponseEntity.ok(new PortalResponse(url));
  }

  // ── Webhook ───────────────────────────────────────────────────────────────

  @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Endpoint webhook signé — appelé par le provider de paiement")
  public ResponseEntity<Void> webhook(HttpServletRequest request) throws IOException {
    byte[] payload = request.getInputStream().readAllBytes();
    Map<String, String> headers = extractHeaders(request);

    BillingEvent event = billingStack.verifyAndParseWebhook(payload, headers);
    webhookHandler.handle(event);
    return ResponseEntity.ok().build();
  }

  // ── Provider info ─────────────────────────────────────────────────────────

  @GetMapping("/provider")
  @Operation(summary = "Retourne le provider de paiement actif")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, String>> provider() {
    return ResponseEntity.ok(Map.of("provider", billingStack.activeProviderCode()));
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private BillingOverviewResponse.QuotaUsage toQuota(
      String metric, Map<String, Long> counters, int limit) {
    long used = counters.getOrDefault(metric, 0L);
    double pct = (limit <= 0) ? -1.0 : (double) used / limit;
    return new BillingOverviewResponse.QuotaUsage(metric, used, limit, pct);
  }

  private Plan defaultFreePlan() {
    Plan p = new Plan();
    p.setCode("FREE"); p.setLabel("Gratuit");
    p.setMaxCampaignsPerMonth(2); p.setMaxEmailsPerMonth(1000);
    p.setMaxSmsPerMonth(0); p.setMaxWhatsappPerMonth(0);
    return p;
  }

  private Map<String, String> extractHeaders(HttpServletRequest request) {
    Map<String, String> map = new HashMap<>();
    Enumeration<String> names = request.getHeaderNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      map.put(name.toLowerCase(), request.getHeader(name));
    }
    return map;
  }
}
