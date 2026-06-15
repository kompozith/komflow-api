package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;

/**
 * Port (interface domaine) qu'implémente tout adaptateur de canal de messagerie.
 * <p>
 * L'ajout d'un nouveau provider (ex: Infobip, AWS SNS, Vonage) se fait en créant
 * un @Service qui implémente cette interface ; aucune modification du dispatcher n'est requise.
 */
public interface ChannelProviderPort {

  /**
   * Code unique identifiant ce provider (ex : META, TWILIO, AWS_SNS, INFOBIP).
   * Utilisé dans les logs, métriques et règles de routing.
   */
  String providerCode();

  /**
   * Indique si ce provider supporte le canal demandé.
   */
  boolean supports(MessageChannel channel);

  /**
   * Santé observée du provider : succès récent, nombre d'erreurs, latence.
   * Utilisée par le RoutingPolicyEngine pour écarter un provider dégradé.
   */
  ProviderHealth health();

  /**
   * Envoie un message via ce provider.
   *
   * @param command commande normalisée (destinataire, contenu, pièces jointes, metadata)
   * @return résultat de l'envoi avec messageId provider et statut
   * @throws ChannelProviderException si le provider rejette l'envoi (non-réessayable)
   * @throws RuntimeException        si l'envoi échoue de manière réessayable
   */
  ChannelSendResult send(ChannelSendCommand command);

  // ── Inner records ───────────────────────────────────────────────────────────

  record ChannelSendCommand(
    String toAddress,
    String content,
    MessageChannel channel,
    java.util.List<String> attachmentUrls,
    java.util.Map<String, String> metadata
  ) {}

  record ChannelSendResult(
    boolean success,
    String providerMessageId,
    String providerCode,
    MessageChannel channel,
    String errorDetail
  ) {}

  record ProviderHealth(
    boolean available,
    int recentErrorCount,
    long avgLatencyMs
  ) {}

  class ChannelProviderException extends RuntimeException {
    public ChannelProviderException(String message) { super(message); }
    public ChannelProviderException(String message, Throwable cause) { super(message, cause); }
  }
}
