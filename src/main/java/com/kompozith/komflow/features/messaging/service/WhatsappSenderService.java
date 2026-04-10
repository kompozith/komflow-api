package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WhatsappSenderService extends MessageSender {

    private static final long MAX_WHATSAPP_ATTACHMENT_BYTES = 30L * 1024 * 1024; // 30 MB

    /** Extension → MIME type map for common media files sent via WhatsApp. */
    private static final Map<String, String> MIME_BY_EXT = Map.ofEntries(
            Map.entry("jpg",  "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png",  "image/png"),
            Map.entry("gif",  "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("mp4",  "video/mp4"),
            Map.entry("3gp",  "video/3gpp"),
            Map.entry("mp3",  "audio/mpeg"),
            Map.entry("ogg",  "audio/ogg"),
            Map.entry("amr",  "audio/amr"),
            Map.entry("pdf",  "application/pdf"),
            Map.entry("doc",  "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls",  "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("ppt",  "application/vnd.ms-powerpoint"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation")
    );

    private final WhatsAppPort whatsAppPort;

    public WhatsappSenderService(WhatsAppPort whatsAppPort) {
        super(MessageChannel.WHATSAPP);
        this.whatsAppPort = whatsAppPort;
    }

    @Override
    protected void sendPersonalizedMessage(Contact contact, Message message, String personalizedContent) {
        if (!validateContact(contact)) {
            throw new IllegalArgumentException("Contact must have a valid WhatsApp phone number");
        }

        String toNumber = getRecipientIdentifier(contact);

        try {
            List<File> attachments = message.getAttachments();
            int attachmentCount = attachments != null ? attachments.size() : 0;
            log.info("WhatsApp send to {}: content length={}, attachments={}", toNumber,
                    personalizedContent != null ? personalizedContent.length() : 0, attachmentCount);

            if (CollectionUtils.isEmpty(attachments)) {
                // Text-only message (or template)
                String messageId = whatsAppPort.sendTextMessage(toNumber, personalizedContent);
                logSending(contact, message, true, null);
                log.info("WhatsApp text message sent to {} – wamid: {}", toNumber, messageId);
            } else {
                // Send the text body first, then each attachment as a media message
                if (!personalizedContent.isBlank()) {
                    whatsAppPort.sendTextMessage(toNumber, personalizedContent);
                }
                for (File attachment : attachments) {
                    sendAttachment(toNumber, attachment);
                }
                logSending(contact, message, true, null);
                log.info("WhatsApp message with {} attachment(s) sent to {}", attachments.size(), toNumber);
            }

        } catch (WhatsAppSendException e) {
            logSending(contact, message, false, e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        } catch (Exception e) {
            logSending(contact, message, false, e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }

    private void sendAttachment(String toNumber, File attachment) {
        String url = attachment.getUrl();
        if (url == null || url.isBlank()) {
            log.warn("Skipping attachment '{}' — URL is missing", attachment.getName());
            return;
        }
        if (!url.startsWith("https://")) {
            log.warn("Attachment '{}' URL is not HTTPS ({}). Meta WhatsApp Cloud API requires a publicly accessible HTTPS URL. " +
                    "The attachment may be rejected.", attachment.getName(), url);
        }
        String mimeType = guessMimeType(attachment.getName());
        log.debug("Sending WhatsApp attachment '{}' (mime={}) to {} from URL: {}", attachment.getName(), mimeType, toNumber, url);
        whatsAppPort.sendMediaMessage(toNumber, url, mimeType, null, attachment.getName());
    }

    private String guessMimeType(String filename) {
        if (filename == null) return "application/octet-stream";
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "application/octet-stream";
        String ext = filename.substring(dot + 1).toLowerCase();
        return MIME_BY_EXT.getOrDefault(ext, "application/octet-stream");
    }

    @Override
    protected boolean validateContact(Contact contact) {
        if (contact.getPerson() == null || contact.getPerson().getPhoneNumbers() == null) {
            return false;
        }

        return contact.getPerson().getPhoneNumbers().stream()
                .anyMatch(phone -> "true".equalsIgnoreCase(phone.getIsWhatsapp()) && phone.getNumber() != null);
    }

    @Override
    protected String getRecipientIdentifier(Contact contact) {
        return contact.getPerson().getPhoneNumbers().stream()
                .filter(phone -> "true".equalsIgnoreCase(phone.getIsWhatsapp()))
                .findFirst()
                .map(phone -> phone.getNumber())
                .orElse(null);
    }

    @Override
    protected String prepareContent(Message message) {
        return message.getContent();
    }
}