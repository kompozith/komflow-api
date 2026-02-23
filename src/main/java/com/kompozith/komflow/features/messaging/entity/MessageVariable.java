package com.kompozith.komflow.features.messaging.entity;

import lombok.Getter;

@Getter
public enum MessageVariable {
    // Person fields
    FIRST_NAME("{{firstName}}", "person.firstName", "Prenom du contact"),
    LAST_NAME("{{lastName}}", "person.lastName", "Nom de famille du contact"),
    EMAIL("{{email}}", "person.email", "Adresse email du contact"),
    LANGUAGE("{{language}}", "person.language", "Langue preferee du contact"),
    COUNTRY("{{country}}", "person.country", "Pays du contact"),
    CITY("{{city}}", "person.city", "Ville du contact"),
    EVENT_LOCAL_TIME("{{eventLocalTime}}", "__event.localTime__", "Date et heure de l'evenement au fuseau du contact"),
    EVENT_END_LOCAL_TIME("{{eventEndLocalTime}}", "__event.endLocalTime__", "Date et heure de fin de l'evenement au fuseau du contact"),
    EVENT_TITLE("{{eventTitle}}", "__event.title__", "Titre de l'evenement lie au message"),
    EVENT_START_DATE("{{eventStartDate}}", "__event.startDate__", "Date locale de debut de l'evenement"),
    EVENT_START_TIME("{{eventStartTime}}", "__event.startTime__", "Heure locale de debut de l'evenement"),
    EVENT_END_DATE("{{eventEndDate}}", "__event.endDate__", "Date locale de fin de l'evenement"),
    EVENT_END_TIME("{{eventEndTime}}", "__event.endTime__", "Heure locale de fin de l'evenement"),
    EVENT_LOCATION("{{eventLocation}}", "__event.location__", "Lieu de l'evenement"),
    EVENT_TIMEZONE("{{eventTimezone}}", "__event.timezone__", "Fuseau horaire de l'evenement"),

    // Contact fields
    CONTACT_ID("{{contactId}}", "id", "Identifiant unique du contact"),
    CONTACT_ENABLED("{{contactEnabled}}", "enabled", "Statut d'activation du contact"),

    // User fields (if linked)
    USERNAME("{{username}}", "person.user.username", "Nom d'utilisateur (si lie a un compte)"),

    // Phone numbers (first available phone)
    PHONE_NUMBER("{{phoneNumber}}", "person.phoneNumbers[0].number", "Premier numero de telephone disponible"),

    // WhatsApp phone (first WhatsApp-enabled phone)
    WHATSAPP_NUMBER("{{whatsappNumber}}", "person.phoneNumbers[whatsapp].number", "Numero WhatsApp (si disponible)");

    private final String key;
    private final String fieldPath;
    private final String description;

    MessageVariable(String key, String fieldPath, String description) {
        this.key = key;
        this.fieldPath = fieldPath;
        this.description = description;
    }

    public static MessageVariable fromKey(String key) {
        for (MessageVariable variable : values()) {
            if (variable.key.equalsIgnoreCase(key)) {
                return variable;
            }
        }
        return null;
    }
}
