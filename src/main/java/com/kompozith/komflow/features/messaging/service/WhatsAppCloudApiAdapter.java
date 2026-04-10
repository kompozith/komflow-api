package com.kompozith.komflow.features.messaging.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kompozith.komflow.configuration.WhatsAppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Adaptateur infrastructure : implémente {@link WhatsAppPort} en appelant
 * l'API WhatsApp Cloud de Meta (Graph API v25.0+).
 *
 * <p>Deux modes d'envoi sont supportés :
 * <ul>
 *   <li><b>Template</b> (recommandé pour les campagnes) : si {@code app.whatsapp.template-name}
 *       est configuré, envoie un message de type {@code template} — aucune fenêtre de 24h.
 *       Le template doit avoir un composant BODY avec une variable {@code {{1}}}.</li>
 *   <li><b>Text</b> (fenêtre 24h) : utilisé si aucun template n'est configuré.
 *       Fonctionne uniquement quand le contact a initié une conversation récente.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppCloudApiAdapter implements WhatsAppPort {

    private final WhatsAppProperties whatsAppProperties;
    private final RestTemplate whatsappRestTemplate;

    /**
     * {@inheritDoc}
     *
     * <p>Choisit automatiquement le mode template ou text selon la configuration.
     */
    @Override
    public String sendTextMessage(String toPhoneNumber, String textBody) {
        String url = whatsAppProperties.getMessagesUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsAppProperties.getAccessToken());

        boolean useTemplate = StringUtils.hasText(whatsAppProperties.getTemplateName());
        Map<String, Object> requestBody = useTemplate
                ? buildTemplateMessagePayload(toPhoneNumber, textBody)
                : buildTextMessagePayload(toPhoneNumber, textBody);

        log.debug("Sending WhatsApp {} message to {} via {}",
                useTemplate ? "template[" + whatsAppProperties.getTemplateName() + "]" : "text",
                toPhoneNumber, url);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<WhatsAppSendResponse> response =
                    whatsappRestTemplate.exchange(url, HttpMethod.POST, request, WhatsAppSendResponse.class);

            WhatsAppSendResponse body = response.getBody();
            String messageId = extractMessageId(body);
            log.info("WhatsApp message sent to {} – wamid: {}", toPhoneNumber, messageId);
            return messageId;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String apiError = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
            boolean isUnauthorized = ex.getStatusCode().value() == 401;
            log.error("WhatsApp API error (HTTP {}) sending to {}: {}{}",
                    ex.getStatusCode(), toPhoneNumber, apiError,
                    isUnauthorized ? " — access token is likely expired or invalid. Regenerate it in Meta Business Platform." : "");
            throw new WhatsAppSendException(
                    "WhatsApp API rejected the message for " + toPhoneNumber
                            + (isUnauthorized ? " (401 Unauthorized — token expired)" : ""),
                    ex.getStatusCode().value(),
                    apiError
            );
        } catch (Exception ex) {
            log.error("Unexpected error sending WhatsApp message to {}: {}", toPhoneNumber, ex.getMessage(), ex);
            throw new WhatsAppSendException("Failed to send WhatsApp message to " + toPhoneNumber, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendMediaMessage(String toPhoneNumber, String mediaUrl, String mimeType,
                                   String caption, String filename) {
        String url = whatsAppProperties.getMessagesUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsAppProperties.getAccessToken());

        Map<String, Object> requestBody = buildMediaMessagePayload(toPhoneNumber, mediaUrl, mimeType, caption, filename);

        log.debug("Sending WhatsApp media ({}) to {} via {}", resolveWhatsAppMediaType(mimeType), toPhoneNumber, url);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<WhatsAppSendResponse> response =
                    whatsappRestTemplate.exchange(url, HttpMethod.POST, request, WhatsAppSendResponse.class);
            String messageId = extractMessageId(response.getBody());
            log.info("WhatsApp media sent to {} – wamid: {}", toPhoneNumber, messageId);
            return messageId;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String apiError = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
            boolean isUnauthorized = ex.getStatusCode().value() == 401;
            log.error("WhatsApp API error (HTTP {}) sending media to {}: {}{}",
                    ex.getStatusCode(), toPhoneNumber, apiError,
                    isUnauthorized ? " — access token is likely expired or invalid. Regenerate it in Meta Business Platform." : "");
            throw new WhatsAppSendException(
                    "WhatsApp API rejected the media message for " + toPhoneNumber
                            + (isUnauthorized ? " (401 Unauthorized — token expired)" : ""),
                    ex.getStatusCode().value(),
                    apiError
            );
        } catch (Exception ex) {
            log.error("Unexpected error sending WhatsApp media to {}: {}", toPhoneNumber, ex.getMessage(), ex);
            throw new WhatsAppSendException("Failed to send WhatsApp media to " + toPhoneNumber, ex);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Payload builders
    // ──────────────────────────────────────────────────────────────

    /**
     * Construit un payload de type {@code text} — fenêtre de session 24h requise.
     */
    private Map<String, Object> buildTextMessagePayload(String to, String body) {
        return Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "text",
                "text", Map.of(
                        "preview_url", false,
                        "body", body
                )
        );
    }

    /**
     * Construit un payload de type {@code template}.
     * Le contenu du message est passé dans la variable {@code {{1}}} du composant BODY.
     * Fonctionne hors fenêtre 24h — le template doit être approuvé par Meta.
     */
    private Map<String, Object> buildTemplateMessagePayload(String to, String bodyText) {
        Map<String, Object> bodyComponent = Map.of(
                "type", "body",
                "parameters", List.of(
                        Map.of("type", "text", "text", bodyText)
                )
        );

        return Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", whatsAppProperties.getTemplateName(),
                        "language", Map.of("code", whatsAppProperties.getTemplateLanguage()),
                        "components", List.of(bodyComponent)
                )
        );
    }

    /**
     * Résout le type WhatsApp ("image", "video", "audio", "document") à partir du MIME type.
     */
    private String resolveWhatsAppMediaType(String mimeType) {
        if (mimeType == null) return "document";
        String lower = mimeType.toLowerCase();
        if (lower.startsWith("image/")) return "image";
        if (lower.startsWith("video/")) return "video";
        if (lower.startsWith("audio/")) return "audio";
        return "document";
    }

    /**
     * Construit un payload média (image / vidéo / audio / document).
     */
    private Map<String, Object> buildMediaMessagePayload(
            String to, String mediaUrl, String mimeType, String caption, String filename) {

        String mediaType = resolveWhatsAppMediaType(mimeType);

        java.util.Map<String, Object> mediaObject = new java.util.LinkedHashMap<>();
        mediaObject.put("link", mediaUrl);
        if (caption != null && !caption.isBlank() && !"audio".equals(mediaType)) {
            mediaObject.put("caption", caption);
        }
        if ("document".equals(mediaType) && filename != null && !filename.isBlank()) {
            mediaObject.put("filename", filename);
        }

        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", to);
        payload.put("type", mediaType);
        payload.put(mediaType, mediaObject);
        return payload;
    }

    // ──────────────────────────────────────────────────────────────
    // Response parsing
    // ──────────────────────────────────────────────────────────────

    private String extractMessageId(WhatsAppSendResponse body) {
        if (body != null
                && body.messages() != null
                && !body.messages().isEmpty()) {
            return body.messages().get(0).id();
        }
        return "unknown";
    }

    // ──────────────────────────────────────────────────────────────
    // Response records
    // ──────────────────────────────────────────────────────────────

    record WhatsAppSendResponse(
            @JsonProperty("messaging_product") String messagingProduct,
            @JsonProperty("contacts") List<WhatsAppContact> contacts,
            @JsonProperty("messages") List<WhatsAppMessageRef> messages
    ) {}

    record WhatsAppContact(
            @JsonProperty("input") String input,
            @JsonProperty("wa_id") String waId
    ) {}

    record WhatsAppMessageRef(
            @JsonProperty("id") String id
    ) {}
}
