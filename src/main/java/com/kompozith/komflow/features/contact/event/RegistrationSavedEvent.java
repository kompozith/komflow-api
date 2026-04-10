package com.kompozith.komflow.features.contact.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Événement publié après la validation en base d'une inscription publique à un événement.
 * Déclenché dans afterCommit() pour garantir la cohérence des données avant notification SSE.
 */
@Getter
public class RegistrationSavedEvent extends ApplicationEvent {

    private final Long eventId;
    private final Long contactId;

    public RegistrationSavedEvent(Object source, Long eventId, Long contactId) {
        super(source);
        this.eventId = eventId;
        this.contactId = contactId;
    }
}
