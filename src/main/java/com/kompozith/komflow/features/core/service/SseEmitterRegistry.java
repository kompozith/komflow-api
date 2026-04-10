package com.kompozith.komflow.features.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry thread-safe pour gérer les connexions SSE par eventId.
 * Chaque client connecté reçoit un SseEmitter individuel.
 * Les emitters morts (timeout, erreur, fermeture) sont retirés automatiquement.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SseEmitterRegistry {

    private static final long SSE_TIMEOUT_MS = 10 * 60 * 1000L; // 10 minutes

    private final ObjectMapper objectMapper;

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Enregistre un nouveau SseEmitter pour un eventId donné.
     * Configure les callbacks de nettoyage automatique.
     *
     * @param eventId identifiant de l'événement
     * @return un SseEmitter actif, prêt à recevoir des messages
     */
    public SseEmitter register(Long eventId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        List<SseEmitter> list = emitters.computeIfAbsent(eventId, id -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        emitter.onCompletion(() -> remove(eventId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(eventId, emitter);
        });
        emitter.onError(ex -> {
            emitter.completeWithError(ex);
            remove(eventId, emitter);
        });

        return emitter;
    }

    /**
     * Diffuse un payload JSON à tous les clients SSE connectés sur un eventId.
     * Les emitters morts sont retirés silencieusement.
     *
     * @param eventId identifiant de l'événement
     * @param payload objet à sérialiser et envoyer
     */
    public void broadcast(Long eventId, Object payload) {
        List<SseEmitter> list = emitters.get(eventId);
        if (list == null || list.isEmpty()) {
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("SSE broadcast: failed to serialize payload for eventId {}: {}", eventId, e.getMessage());
            return;
        }

        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().data(json));
            } catch (IOException | IllegalStateException e) {
                dead.add(emitter);
            }
        }

        list.removeAll(dead);
        if (list.isEmpty()) {
            emitters.remove(eventId);
        }
    }

    private void remove(Long eventId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(eventId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(eventId);
            }
        }
    }
}
