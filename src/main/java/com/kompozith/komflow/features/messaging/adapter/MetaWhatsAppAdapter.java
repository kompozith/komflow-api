package com.kompozith.komflow.features.messaging.adapter;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.service.ChannelProviderPort;
import com.kompozith.komflow.features.messaging.service.WhatsAppPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptateur ChannelProviderPort pour WhatsApp via Meta Cloud API.
 * Délègue à {@link WhatsAppPort} (déjà implémenté par WhatsAppCloudApiAdapter).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetaWhatsAppAdapter implements ChannelProviderPort {

  private final WhatsAppPort whatsAppPort;

  private final AtomicInteger recentErrors = new AtomicInteger(0);
  private final AtomicLong totalLatencyMs = new AtomicLong(0);
  private final AtomicInteger callCount = new AtomicInteger(0);

  @Override
  public String providerCode() {
    return "META_WHATSAPP";
  }

  @Override
  public boolean supports(MessageChannel channel) {
    return channel == MessageChannel.WHATSAPP;
  }

  @Override
  public ProviderHealth health() {
    int calls = callCount.get();
    long avgLatency = calls > 0 ? totalLatencyMs.get() / calls : 0L;
    return new ProviderHealth(recentErrors.get() < 5, recentErrors.get(), avgLatency);
  }

  @Override
  public ChannelSendResult send(ChannelSendCommand command) {
    long start = System.currentTimeMillis();
    try {
      String wamid = whatsAppPort.sendTextMessage(command.toAddress(), command.content());
      recordSuccess(start);
      return new ChannelSendResult(true, wamid, providerCode(), command.channel(), null);
    } catch (Exception e) {
      recordError();
      log.error("[MetaWhatsAppAdapter] send failed to {}: {}", command.toAddress(), e.getMessage());
      return new ChannelSendResult(false, null, providerCode(), command.channel(), e.getMessage());
    }
  }

  private void recordSuccess(long start) {
    recentErrors.set(Math.max(0, recentErrors.get() - 1));
    totalLatencyMs.addAndGet(System.currentTimeMillis() - start);
    callCount.incrementAndGet();
  }

  private void recordError() {
    recentErrors.incrementAndGet();
    callCount.incrementAndGet();
  }
}
