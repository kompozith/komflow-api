package com.kompozith.komflow.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de connexion à l'API WhatsApp Cloud (Meta Business Platform).
 * Chargées depuis le bloc {@code app.whatsapp} des fichiers application-*.yml.
 */
@Component
@ConfigurationProperties(prefix = "app.whatsapp")
@Getter
@Setter
public class WhatsAppProperties {

    /** Numéro WhatsApp émetteur au format E.164 (ex: +15556369714). */
    private String phoneNumber;

    /** Identifiant du numéro de téléphone dans la plateforme Meta. */
    private String phoneNumberId;

    /** Identifiant du compte WhatsApp Business. */
    private String businessAccountId;

    /** Token d'accès permanent (ou longue durée) Meta Graph API. */
    private String accessToken;

    /** Version de l'API Graph à utiliser (ex: v25.0). */
    private String apiVersion = "v25.0";

    /**
     * Nom du template approuvé utilisé pour les messages hors-fenêtre (campagnes).
     * Le template doit avoir un composant BODY avec une variable {{1}}.
     * Laisser vide pour envoyer en mode text-only (fenêtre 24h uniquement).
     */
    private String templateName;

    /** Code langue du template (ex: fr, en_US). Défaut : fr. */
    private String templateLanguage = "fr";

    /** Timeout de connexion HTTP en ms. */
    private int connectTimeoutMs = 10_000;

    /** Timeout de lecture HTTP en ms. */
    private int readTimeoutMs = 15_000;

    /**
     * Construit l'URL de base pour l'envoi de messages.
     * ex : https://graph.facebook.com/v18.0/1031724846693018/messages
     */
    public String getMessagesUrl() {
        return "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";
    }
}
