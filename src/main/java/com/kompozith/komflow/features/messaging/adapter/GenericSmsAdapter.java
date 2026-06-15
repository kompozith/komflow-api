package com.kompozith.komflow.features.messaging.adapter;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.service.ChannelProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptateur SMS provider-agnostic.
 * <p>
 * Par défaut, supporte tout provider HTTP REST exposant une API JSON.
 * Le provider actif est sélectionné via {@code app.sms.provider-type} :
 * <ul>
 *   <li>{@code STUB} (défaut) — log uniquement, pas d'envoi réel</li>
 *   <li>{@code HTTP_REST} — appel vers {@code app.sms.api-url} avec Bearer token</li>
 * </ul>
 * Pour brancher Twilio, Infobip, Vonage, Orange ou tout autre provider SMS,
 * il suffit de créer un nouveau @Service qui implémente {@link ChannelProviderPort}
 * sans toucher à ce fichier.
 */
@Service
@Slf4j
public class GenericSmsAdapter implements ChannelProviderPort {

  @Value("${app.sms.provider-type:STUB}")
  private String providerType;

  @Value("${app.sms.api-url:}")
  private String apiUrl;

  @Value("${app.sms.api-token:}")
  private String apiToken;

  @Value("${app.sms.from-number:}")
  private String fromNumber;

  private final RestTemplate restTemplate = new RestTemplate();
  private final AtomicInteger recentErrors = new AtomicInteger(0);
  private final AtomicLong totalLatencyMs = new AtomicLong(0);
  private final AtomicInteger callCount = new AtomicInteger(0);

  @Override
  public String providerCode() {
    return "SMS_" + providerType;
  }

  @Override
  public boolean supports(MessageChannel channel) {
    return channel == MessageChannel.SMS;
  }

  @Override
  public ProviderHealth health() {
    int calls = callCount.get();
    long avgLatency = calls > 0 ? totalLatencyMs.get() / calls : 0L;
    return new ProviderHealth(recentErrors.get() < 5, recentErrors.get(), avgLatency);
  }

  @Override
  public ChannelSendResult send(ChannelSendCommand command) {
    if ("STUB".equalsIgnoreCase(providerType)) {
      return sendStub(command);
    }
    return sendViaHttpRest(command);
  }

  // ── STUB ────────────────────────────────────────────────────────────────────

  private ChannelSendResult sendStub(ChannelSendCommand command) {
    log.info("[GenericSmsAdapter][STUB] SMS would be sent to {} ({}): {}",
      command.toAddress(), fromNumber, abbreviate(command.content(), 80));
    callCount.incrementAndGet();
    return new ChannelSendResult(true, "stub-" + System.currentTimeMillis(),
      providerCode(), command.channel(), null);
  }

  // ── HTTP REST generic ────────────────────────────────────────────────────────

  private ChannelSendResult sendViaHttpRest(ChannelSendCommand command) {
    if (apiUrl == null || apiUrl.isBlank()) {
      throw new ChannelProviderException("app.sms.api-url is not configured");
    }
    long start = System.currentTimeMillis();
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      if (apiToken != null && !apiToken.isBlank()) {
        headers.setBearerAuth(apiToken);
      }
      Map<String, String> body = Map.of(
        "to", command.toAddress(),
        "from", fromNumber,
        "text", command.content()
      );
      HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

      boolean success = response.getStatusCode().is2xxSuccessful();
      String msgId = extractMessageId(response.getBody());
      recordResult(success, start);
      return new ChannelSendResult(success, msgId, providerCode(), command.channel(),
        success ? null : "HTTP " + response.getStatusCode());
    } catch (Exception e) {
      recordError();
      log.error("[GenericSmsAdapter][HTTP_REST] send failed to {}: {}", command.toAddress(), e.getMessage());
      return new ChannelSendResult(false, null, providerCode(), command.channel(), e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private String extractMessageId(Map<?, ?> body) {
    if (body == null) return null;
    Object id = body.get("messageId");
    if (id == null) id = body.get("id");
    if (id == null) id = body.get("message_id");
    return id != null ? id.toString() : null;
  }

  private void recordResult(boolean success, long start) {
    if (!success) recentErrors.incrementAndGet();
    else recentErrors.set(Math.max(0, recentErrors.get() - 1));
    totalLatencyMs.addAndGet(System.currentTimeMillis() - start);
    callCount.incrementAndGet();
  }

  private void recordError() {
    recentErrors.incrementAndGet();
    callCount.incrementAndGet();
  }

  private String abbreviate(String s, int max) {
    if (s == null) return "";
    return s.length() <= max ? s : s.substring(0, max) + "…";
  }
}
