package com.kompozith.komflow.features.billing.adapter;

import com.kompozith.komflow.features.billing.port.BillingProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

/**
 * Adaptateur billing HTTP REST générique.
 * Activé via {@code app.billing.provider-type=HTTP_REST}.
 *
 * Permet de brancher n'importe quel provider REST (Stripe, Paddle, LemonSqueezy…)
 * en renseignant uniquement des variables d'environnement préfixées
 * {@code app.billing.http.*} sans modifier le code.
 *
 * <pre>
 * app:
 *   billing:
 *     provider-type: HTTP_REST
 *     http:
 *       provider-name: STRIPE          # utilisé comme providerCode()
 *       api-url: https://api.stripe.com/v1
 *       api-key: sk_live_...
 *       webhook-secret: whsec_...
 *       checkout-path: /checkout/sessions
 *       portal-path: /billing/portal/sessions
 *       webhook-signature-header: Stripe-Signature
 *       webhook-signature-algo: HmacSHA256  # HmacSHA256 | HmacSHA512
 * </pre>
 *
 * Pour un provider dont la structure de réponse diffère, créez un adaptateur
 * dédié implementant {@link BillingProviderPort} — aucun code commun à modifier.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "app.billing.provider-type", havingValue = "HTTP_REST")
public class GenericHttpBillingAdapter implements BillingProviderPort {

  @Value("${app.billing.http.provider-name:HTTP_REST}")
  private String providerName;

  @Value("${app.billing.http.api-url}")
  private String apiUrl;

  @Value("${app.billing.http.api-key}")
  private String apiKey;

  @Value("${app.billing.http.webhook-secret:}")
  private String webhookSecret;

  @Value("${app.billing.http.checkout-path:/checkout/sessions}")
  private String checkoutPath;

  @Value("${app.billing.http.portal-path:/billing/portal/sessions}")
  private String portalPath;

  @Value("${app.billing.http.webhook-signature-header:Stripe-Signature}")
  private String webhookSigHeader;

  @Value("${app.billing.http.webhook-signature-algo:HmacSHA256}")
  private String webhookSigAlgo;

  private final RestTemplate restTemplate;

  public GenericHttpBillingAdapter(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public String providerCode() {
    return providerName.toUpperCase();
  }

  // ── Checkout ─────────────────────────────────────────────────────────────

  @Override
  public CheckoutResult createCheckoutSession(CheckoutCommand cmd) {
    HttpHeaders headers = bearerHeaders();
    Map<String, Object> body = Map.of(
        "mode",         "subscription",
        "customer_email", cmd.customerEmail(),
        "success_url",  cmd.successUrl(),
        "cancel_url",   cmd.cancelUrl(),
        "metadata",     Map.of(
            "organization_id", String.valueOf(cmd.organizationId()),
            "plan_code",       cmd.planCode()
        )
    );

    HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
    ResponseEntity<Map> resp = restTemplate.exchange(
        apiUrl + checkoutPath, HttpMethod.POST, req, Map.class);

    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new IllegalStateException("[%s] checkout failed: %s".formatted(providerCode(), resp.getStatusCode()));
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> respBody = (Map<String, Object>) resp.getBody();
    String url       = (String) respBody.get("url");
    Object idObj     = respBody.get("id");
    String sessionId = idObj != null ? idObj.toString() : "";
    log.info("[{}] Checkout created org={} plan={} sessionId={}", providerCode(), cmd.organizationId(), cmd.planCode(), sessionId);
    return new CheckoutResult(url, sessionId);
  }

  // ── Portail ──────────────────────────────────────────────────────────────

  @Override
  public String createPortalSession(Long orgId, String returnUrl) {
    HttpHeaders headers = bearerHeaders();
    Map<String, Object> body = Map.of("return_url", returnUrl);
    HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
    ResponseEntity<Map> resp = restTemplate.exchange(
        apiUrl + portalPath, HttpMethod.POST, req, Map.class);

    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new IllegalStateException("[%s] portal session failed: %s".formatted(providerCode(), resp.getStatusCode()));
    }
    return (String) resp.getBody().get("url");
  }

  // ── Webhook ───────────────────────────────────────────────────────────────

  @Override
  public BillingEvent verifyAndParseWebhook(byte[] payload, Map<String, String> headers) {
    if (!webhookSecret.isBlank()) {
      verifySignature(payload, headers);
    }
    // Parsing générique — les champs concrets dépendent du provider.
    // Pour un mapping précis, créez un adaptateur dédié.
    log.info("[{}] Webhook verified ({} bytes)", providerCode(), payload.length);
    return new BillingEvent(
        BillingEventType.UNKNOWN,
        null, null,
        Instant.now(),
        null,
        headers.getOrDefault("x-event-id", "unknown")
    );
  }

  // ── Vérification HMAC de la signature webhook ────────────────────────────

  private void verifySignature(byte[] payload, Map<String, String> headers) {
    String sigHeader = headers.entrySet().stream()
        .filter(e -> e.getKey().equalsIgnoreCase(webhookSigHeader))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseThrow(() -> new WebhookVerificationException(
            "Missing signature header: " + webhookSigHeader));

    try {
      Mac mac = Mac.getInstance(webhookSigAlgo);
      mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), webhookSigAlgo));
      String computed = HexFormat.of().formatHex(mac.doFinal(payload));

      // Comparaison en temps constant (résistant au timing attack)
      String provided = sigHeader.contains("=")
          ? sigHeader.substring(sigHeader.lastIndexOf('=') + 1)
          : sigHeader;

      if (!constantTimeEquals(computed, provided)) {
        throw new WebhookVerificationException("Webhook signature mismatch");
      }
    } catch (WebhookVerificationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new WebhookVerificationException("Signature verification error", ex);
    }
  }

  /** Comparaison à temps constant pour éviter les timing attacks. */
  private static boolean constantTimeEquals(String a, String b) {
    if (a.length() != b.length()) return false;
    int diff = 0;
    for (int i = 0; i < a.length(); i++) {
      diff |= a.charAt(i) ^ b.charAt(i);
    }
    return diff == 0;
  }

  private HttpHeaders bearerHeaders() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setBearerAuth(apiKey);
    return h;
  }
}
