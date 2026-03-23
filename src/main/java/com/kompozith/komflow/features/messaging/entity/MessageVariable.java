package com.kompozith.komflow.features.messaging.entity;

import lombok.Getter;

@Getter
public enum MessageVariable {
    // Person fields
    FIRST_NAME("{{contact_first_name}}", "person.firstName", "Prenom du contact"),
    LAST_NAME("{{contact_last_name}}", "person.lastName", "Nom de famille du contact"),
    EMAIL("{{contact_email}}", "person.email", "Adresse email du contact"),
    LANGUAGE("{{contact_language}}", "person.language", "Langue preferee du contact"),
    COUNTRY("{{contact_country}}", "person.country", "Pays du contact"),
    CITY("{{contact_city}}", "person.city", "Ville du contact"),

    // Registrant aliases (useful for admin notifications in event workflows)
    SUBSCRIBER_FIRST_NAME("{{subscriber_first_name}}", "person.firstName", "Prenom de la personne inscrite"),
    SUBSCRIBER_LAST_NAME("{{subscriber_last_name}}", "person.lastName", "Nom de famille de la personne inscrite"),
    SUBSCRIBER_EMAIL("{{subscriber_email}}", "person.email", "Adresse email de la personne inscrite"),
    SUBSCRIBER_LANGUAGE("{{subscriber_language}}", "person.language", "Langue preferee de la personne inscrite"),
    SUBSCRIBER_COUNTRY("{{subscriber_country}}", "person.country", "Pays de la personne inscrite"),
    SUBSCRIBER_CITY("{{subscriber_city}}", "person.city", "Ville de la personne inscrite"),

    EVENT_LOCAL_TIME("{{event_local_time}}", "__event.localTime__", "Date et heure de l'evenement au fuseau du contact"),
    EVENT_END_LOCAL_TIME("{{event_end_local_time}}", "__event.endLocalTime__", "Date et heure de fin de l'evenement au fuseau du contact"),
    EVENT_TITLE("{{event_title}}", "__event.title__", "Titre de l'evenement lie au message"),
    EVENT_START_DATE("{{event_start_date}}", "__event.startDate__", "Date locale de debut de l'evenement"),
    EVENT_START_TIME("{{event_start_time}}", "__event.startTime__", "Heure locale de debut de l'evenement"),
    EVENT_END_DATE("{{event_end_date}}", "__event.endDate__", "Date locale de fin de l'evenement"),
    EVENT_END_TIME("{{event_end_time}}", "__event.endTime__", "Heure locale de fin de l'evenement"),
    EVENT_LOCATION("{{event_location}}", "__event.location__", "Lieu de l'evenement"),
    EVENT_TIMEZONE("{{event_timezone}}", "__event.timezone__", "Fuseau horaire de l'evenement"),
    EVENT_SUBTITLE("{{event_subtitle}}", "__event.subtitle__", "Sous-titre de l'evenement"),
    EVENT_ADDRESS("{{event_address}}", "__event.address__", "Adresse detaillee de l'evenement"),
    EVENT_MEETING_URL("{{event_meeting_url}}", "__event.meetingUrl__", "Lien de connexion de l'evenement"),
    EVENT_PUBLIC_URL("{{event_public_url}}", "__event.publicUrl__", "Lien public d'inscription a l'evenement"),

    // Subscriber metadata
    SUBSCRIBER_ID("{{subscriber_id}}", "id", "Identifiant unique de la personne inscrite"),
    SUBSCRIBER_ENABLED("{{subscriber_enabled}}", "enabled", "Statut d'activation de la personne inscrite"),

    // User fields (if linked)
    USERNAME("{{contact_username}}", "person.user.username", "Nom d'utilisateur du contact (si lie a un compte)"),
    SUBSCRIBER_USERNAME("{{subscriber_username}}", "person.user.username", "Nom d'utilisateur de la personne inscrite (si lie a un compte)"),

    // Phone numbers (first available phone)
    PHONE_NUMBER("{{contact_phone_number}}", "person.phoneNumbers[0].number", "Premier numero de telephone du contact"),
    SUBSCRIBER_PHONE_NUMBER("{{subscriber_phone_number}}", "person.phoneNumbers[0].number", "Premier numero de telephone de la personne inscrite"),

    // WhatsApp phone (first WhatsApp-enabled phone)
    WHATSAPP_NUMBER("{{contact_whatsapp_number}}", "person.phoneNumbers[whatsapp].number", "Numero WhatsApp du contact (si disponible)"),
    SUBSCRIBER_WHATSAPP_NUMBER("{{subscriber_whatsapp_number}}", "person.phoneNumbers[whatsapp].number", "Numero WhatsApp de la personne inscrite (si disponible)");

    private final String key;
    private final String fieldPath;
    private final String description;

    MessageVariable(String key, String fieldPath, String description) {
        this.key = key;
        this.fieldPath = fieldPath;
        this.description = description;
    }

    public static MessageVariable fromKey(String key) {
        if (key == null) {
            return null;
        }

        // Backward compatibility for existing templates using legacy camelCase keys.
        String normalizedKey = switch (key) {
            case "{{firstName}}" -> "{{contact_first_name}}";
            case "{{lastName}}" -> "{{contact_last_name}}";
            case "{{email}}" -> "{{contact_email}}";
            case "{{language}}" -> "{{contact_language}}";
            case "{{country}}" -> "{{contact_country}}";
            case "{{city}}" -> "{{contact_city}}";
            case "{{username}}" -> "{{contact_username}}";
            case "{{phoneNumber}}" -> "{{contact_phone_number}}";
            case "{{whatsappNumber}}" -> "{{contact_whatsapp_number}}";
            case "{{eventLocalTime}}" -> "{{event_local_time}}";
            case "{{eventEndLocalTime}}" -> "{{event_end_local_time}}";
            case "{{eventTitle}}" -> "{{event_title}}";
            case "{{eventStartDate}}" -> "{{event_start_date}}";
            case "{{eventStartTime}}" -> "{{event_start_time}}";
            case "{{eventEndDate}}" -> "{{event_end_date}}";
            case "{{eventEndTime}}" -> "{{event_end_time}}";
            case "{{eventLocation}}" -> "{{event_location}}";
            case "{{eventTimezone}}" -> "{{event_timezone}}";
            case "{{eventSubtitle}}" -> "{{event_subtitle}}";
            case "{{eventAddress}}" -> "{{event_address}}";
            case "{{eventMeetingUrl}}" -> "{{event_meeting_url}}";
            case "{{eventPublicUrl}}" -> "{{event_public_url}}";
            default -> key;
        };

        for (MessageVariable variable : values()) {
            if (variable.key.equalsIgnoreCase(normalizedKey)) {
                return variable;
            }
        }
        return null;
    }
}
