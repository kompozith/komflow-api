package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Policy engine qui choisit et ordonne les providers disponibles pour un envoi donné.
 * <p>
 * Critères de ranking (ordre décroissant de priorité) :
 * <ol>
 *   <li>Support du canal</li>
 *   <li>Disponibilité (health.available)</li>
 *   <li>Nombre d'erreurs récentes (moins = mieux)</li>
 *   <li>Latence moyenne (moins = mieux)</li>
 * </ol>
 * Ce bean est remplaçable par une implémentation chargée depuis DB (règles configurables par canal/pays).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingPolicyEngine {

  /**
   * Retourne la liste ordonnée des providers capables d'envoyer la commande.
   * Le premier de la liste est le provider préféré ; les suivants sont des fallbacks.
   */
  public List<ChannelProviderPort> rankProviders(
    ChannelProviderPort.ChannelSendCommand command,
    List<ChannelProviderPort> allProviders
  ) {
    MessageChannel channel = command.channel();

    List<ChannelProviderPort> candidates = allProviders.stream()
      .filter(p -> p.supports(channel))
      .sorted(Comparator
        .comparingInt((ChannelProviderPort p) -> p.health().available() ? 0 : 1)
        .thenComparingInt(p -> p.health().recentErrorCount())
        .thenComparingLong(p -> p.health().avgLatencyMs())
      )
      .toList();

    if (candidates.isEmpty()) {
      log.warn("RoutingPolicyEngine: no provider found for channel {}", channel);
    } else {
      log.debug("RoutingPolicyEngine: {} candidate(s) for channel {} — primary={}",
        candidates.size(), channel, candidates.get(0).providerCode());
    }

    return candidates;
  }
}
