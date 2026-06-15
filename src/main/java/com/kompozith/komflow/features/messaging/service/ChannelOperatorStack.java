package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrateur d'envoi canal — vendor-agnostic.
 * <p>
 * Principe :
 * <pre>
 *   ChannelOperatorStack.dispatch(cmd)
 *     → RoutingPolicyEngine.rankProviders(cmd, providers)  // ordonne
 *     → provider[0].send(cmd)                              // tente
 *     → si echec → provider[1].send(cmd)                  // fallback
 *     → ...
 *     → tous échoués → exception
 * </pre>
 * <p>
 * Pour ajouter un provider (ex : Vonage) il suffit de créer un @Service qui implémente
 * {@link ChannelProviderPort} ; Spring l'injecte automatiquement dans cette liste.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelOperatorStack {

  private final List<ChannelProviderPort> providers;
  private final RoutingPolicyEngine policy;

  /**
   * Dispatche un envoi via le meilleur provider disponible avec fallback automatique.
   *
   * @param command commande normalisée (destinataire, contenu, canal, pièces jointes)
   * @return résultat du premier envoi réussi
   * @throws RuntimeException si aucun provider n'a pu délivrer le message
   */
  public ChannelProviderPort.ChannelSendResult dispatch(ChannelProviderPort.ChannelSendCommand command) {
    List<ChannelProviderPort> ordered = policy.rankProviders(command, providers);

    if (ordered.isEmpty()) {
      throw new RuntimeException(
        "No provider available for channel: " + command.channel());
    }

    Exception lastError = null;

    for (ChannelProviderPort provider : ordered) {
      try {
        log.info("[OperatorStack] Attempting channel={} via provider={}",
          command.channel(), provider.providerCode());
        ChannelProviderPort.ChannelSendResult result = provider.send(command);
        if (result.success()) {
          log.info("[OperatorStack] channel={} delivered via provider={}",
            command.channel(), provider.providerCode());
          return result;
        }
        log.warn("[OperatorStack] provider={} returned failure, trying next", provider.providerCode());
      } catch (ChannelProviderPort.ChannelProviderException e) {
        // Non-réessayable : skip immédiatement au suivant
        log.warn("[OperatorStack] provider={} rejected (non-retryable): {}",
          provider.providerCode(), e.getMessage());
        lastError = e;
      } catch (Exception e) {
        log.warn("[OperatorStack] provider={} failed: {} — falling back",
          provider.providerCode(), e.getMessage());
        lastError = e;
      }
    }

    throw new RuntimeException(
      "All providers failed for channel " + command.channel()
        + (lastError != null ? ": " + lastError.getMessage() : ""),
      lastError
    );
  }
}
