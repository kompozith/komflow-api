package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.dto.CampaignEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class CampaignEventStreamService {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long campaignId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(campaignId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(campaignId, emitter));
        emitter.onTimeout(() -> removeEmitter(campaignId, emitter));
        emitter.onError(e -> removeEmitter(campaignId, emitter));

        return emitter;
    }

    public void emit(Long campaignId, CampaignEventDto event) {
        List<SseEmitter> campaignEmitters = emitters.get(campaignId);
        if (campaignEmitters == null || campaignEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : campaignEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.getType().name())
                        .data(event));
            } catch (IOException e) {
                log.debug("Removing SSE emitter for campaign {} due to IO error: {}", campaignId, e.getMessage());
                removeEmitter(campaignId, emitter);
            }
        }
    }

    private void removeEmitter(Long campaignId, SseEmitter emitter) {
        List<SseEmitter> campaignEmitters = emitters.get(campaignId);
        if (campaignEmitters != null) {
            campaignEmitters.remove(emitter);
            if (campaignEmitters.isEmpty()) {
                emitters.remove(campaignId);
            }
        }
    }
}
