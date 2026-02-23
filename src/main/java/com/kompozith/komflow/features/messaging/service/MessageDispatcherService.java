package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.exception.MissingChannelException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MessageDispatcherService {
    private static final String LEGACY_EVENT_START_AT = "{{eventStartAt}}";
    private static final String LEGACY_EVENT_END_AT = "{{eventEndAt}}";
    private static final String EVENT_START_DATE = "{{eventStartDate}}";
    private static final String EVENT_START_TIME = "{{eventStartTime}}";
    private static final String EVENT_END_DATE = "{{eventEndDate}}";
    private static final String EVENT_END_TIME = "{{eventEndTime}}";

    private final EmailSenderService emailSenderService;
    private final WhatsappSenderService whatsappSenderService;
    private final SmsSenderService smsSenderService;
    private final TemplateParserService templateParserService;
    private final MessageContentParserService messageContentParserService;

    private final Map<MessageChannel, MessageSender> senders;

    public MessageDispatcherService(EmailSenderService emailSenderService,
                                   WhatsappSenderService whatsappSenderService,
                                   SmsSenderService smsSenderService,
                                   TemplateParserService templateParserService,
                                   MessageContentParserService messageContentParserService) {
        this.emailSenderService = emailSenderService;
        this.whatsappSenderService = whatsappSenderService;
        this.smsSenderService = smsSenderService;
        this.templateParserService = templateParserService;
        this.messageContentParserService = messageContentParserService;

        // Initialize the map in constructor
        this.senders = new HashMap<>();
        this.senders.put(MessageChannel.EMAIL, emailSenderService);
        this.senders.put(MessageChannel.WHATSAPP, whatsappSenderService);
        this.senders.put(MessageChannel.SMS, smsSenderService);
    }

    /**
     * Send a message to a contact via the specified channel
     */
    public void sendToContact(Contact contact, Message message, MessageChannel channel) {
        sendToContact(contact, message, channel, null);
    }

    public void sendToContact(Contact contact, Message message, MessageChannel channel, Instant eventInstantUtc) {
        if (channel == null) {
            throw new MissingChannelException("Channel parameter is required");
        }

        MessageSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("Unsupported channel: " + channel);
        }

        log.info("Dispatching message {} to contact {} via channel {}", message.getId(), contact.getId(), channel);

        try {
            // Parse template variables
            String normalizedContent = normalizeLegacyEventVariables(message.getContent());
            String personalizedContent = templateParserService.parseTemplate(normalizedContent, contact, message, eventInstantUtc);
            String channelReadyContent = messageContentParserService.renderForChannel(personalizedContent, channel);

            // Create a personalized message object
            Message personalizedMessage = new Message();
            personalizedMessage.setId(message.getId());
            personalizedMessage.setTitle(message.getTitle());
            personalizedMessage.setContent(channelReadyContent);
            personalizedMessage.setChannel(message.getChannel());
            personalizedMessage.setAttachments(
                    message.getAttachments() != null ? new ArrayList<>(message.getAttachments()) : null
            );

            sender.sendMessage(contact, personalizedMessage);
            log.info("Message sent successfully via {} to contact {}", channel, contact.getId());
        } catch (Exception e) {
            log.error("Failed to send message via {} to contact {}: {}", channel, contact.getId(), e.getMessage());
            throw e;
        }
    }

    private String normalizeLegacyEventVariables(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        return content
                .replace(LEGACY_EVENT_START_AT, EVENT_START_DATE + " " + EVENT_START_TIME)
                .replace(LEGACY_EVENT_END_AT, EVENT_END_DATE + " " + EVENT_END_TIME);
    }

    /**
     * Validate if a contact can receive messages via the specified channel
     */
    public boolean canSendToContact(Contact contact, MessageChannel channel) {
        if (channel == null) {
            return false;
        }

        MessageSender sender = senders.get(channel);
        return sender != null && sender.validateContact(contact);
    }

    /**
     * Get the recipient identifier for a contact and channel
     */
    public String getRecipientIdentifier(Contact contact, MessageChannel channel) {
        if (channel == null) {
            return null;
        }

        MessageSender sender = senders.get(channel);
        return sender != null ? sender.getRecipientIdentifier(contact) : null;
    }
}
