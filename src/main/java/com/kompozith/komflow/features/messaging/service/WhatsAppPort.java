package com.kompozith.komflow.features.messaging.service;

/**
 * Port hexagonal : définit le contrat d'envoi de messages via WhatsApp Cloud API.
 * Toute implémentation concrète (Meta Cloud API, Twilio, mock…) doit respecter
 * cette interface afin de rester découplée du domaine applicatif.
 */
public interface WhatsAppPort {

    /**
     * Envoie un message texte au destinataire indiqué.
     *
     * @param toPhoneNumber numéro destinataire au format E.164 (ex: +33600000000)
     * @param textBody      corps du message en texte brut (HTML déjà strippé)
     * @return identifiant du message WhatsApp retourné par l'API (wamid.xxx…)
     * @throws WhatsAppSendException en cas d'erreur API, réseau ou d'autorisation
     */
    String sendTextMessage(String toPhoneNumber, String textBody);

    /**
     * Envoie un média (image, vidéo, document, audio) au destinataire indiqué.
     *
     * @param toPhoneNumber numéro destinataire au format E.164
     * @param mediaUrl      URL publiquement accessible du fichier
     * @param mimeType      type MIME du fichier (ex: "image/jpeg", "video/mp4", "application/pdf")
     * @param caption       légende optionnelle (peut être null ; ignorée pour les audios)
     * @param filename      nom du fichier affiché pour les documents (peut être null)
     * @return identifiant du message WhatsApp retourné par l'API (wamid.xxx…)
     * @throws WhatsAppSendException en cas d'erreur API, réseau ou d'autorisation
     */
    String sendMediaMessage(String toPhoneNumber, String mediaUrl, String mimeType,
                            String caption, String filename);
}
