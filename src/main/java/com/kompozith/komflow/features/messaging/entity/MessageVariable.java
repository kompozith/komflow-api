package com.kompozith.komflow.features.messaging.entity;

import lombok.Getter;

@Getter
public enum MessageVariable {
    // Person fields
    FIRST_NAME("{{firstName}}", "person.firstName", "Prénom du contact"),
    LAST_NAME("{{lastName}}", "person.lastName", "Nom de famille du contact"),
    EMAIL("{{email}}", "person.email", "Adresse email du contact"),
    LANGUAGE("{{language}}", "person.language", "Langue préférée du contact"),

    // Contact fields
    CONTACT_ID("{{contactId}}", "id", "Identifiant unique du contact"),
    CONTACT_ENABLED("{{contactEnabled}}", "enabled", "Statut d'activation du contact"),

    // User fields (if linked)
    USERNAME("{{username}}", "person.user.username", "Nom d'utilisateur (si lié à un compte)"),

    // Phone numbers (first available phone)
    PHONE_NUMBER("{{phoneNumber}}", "person.phoneNumbers[0].number", "Premier numéro de téléphone disponible"),

    // WhatsApp phone (first WhatsApp-enabled phone)
    WHATSAPP_NUMBER("{{whatsappNumber}}", "person.phoneNumbers[whatsapp].number", "Numéro WhatsApp (si disponible)");

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
            if (variable.key.equals(key)) {
                return variable;
            }
        }
        return null;
    }
}