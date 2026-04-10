package com.kompozith.komflow.features.contact.listener;

import com.kompozith.komflow.features.contact.event.RegistrationSavedEvent;
import com.kompozith.komflow.features.core.service.SseEmitterRegistry;
import com.kompozith.komflow.features.messaging.dto.EventRegistrationStatsDto;
import com.kompozith.komflow.features.messaging.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Écoute les RegistrationSavedEvent et diffuse les statistiques mises à jour
 * à tous les clients SSE connectés sur cet événement.
 *
 * Exécuté hors transaction (afterCommit garantit que les données sont en base).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationSavedEventListener {

    private final EventService eventService;
    private final SseEmitterRegistry sseEmitterRegistry;

    @EventListener
    public void onRegistrationSaved(RegistrationSavedEvent event) {
        try {
            EventRegistrationStatsDto stats = eventService.getRegistrationStats(event.getEventId(), null, null);
            sseEmitterRegistry.broadcast(event.getEventId(), stats);
        } catch (Exception e) {
            log.error("SSE: failed to broadcast registration stats for eventId {}: {}",
                    event.getEventId(), e.getMessage());
        }
    }
}
