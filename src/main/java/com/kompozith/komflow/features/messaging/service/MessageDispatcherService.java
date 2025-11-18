package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.exception.MissingChannelException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MessageDispatcherService {

    private final EmailSenderService emailSenderService;
    private final WhatsappSenderService whatsappSenderService;
    private final SmsSenderService smsSenderService;
    private final TemplateParserService templateParserService;

    private final Map<MessageChannel, MessageSender> senders;

    public MessageDispatcherService(EmailSenderService emailSenderService,
                                   WhatsappSenderService whatsappSenderService,
                                   SmsSenderService smsSenderService,
                                   TemplateParserService templateParserService) {
        this.emailSenderService = emailSenderService;
        this.whatsappSenderService = whatsappSenderService;
        this.smsSenderService = smsSenderService;
        this.templateParserService = templateParserService;

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
            String personalizedContent = templateParserService.parseTemplate(message.getBody(), contact);

            // Create a personalized message object
            Message personalizedMessage = new Message();
            personalizedMessage.setId(message.getId());
            personalizedMessage.setTitle(message.getTitle());
            personalizedMessage.setBody(personalizedContent);
            personalizedMessage.setType(message.getType());
            personalizedMessage.setAttachments(message.getAttachments());

            sender.sendMessage(contact, personalizedMessage);
            log.info("Message sent successfully via {} to contact {}", channel, contact.getId());
        } catch (Exception e) {
            log.error("Failed to send message via {} to contact {}: {}", channel, contact.getId(), e.getMessage());
            throw e;
        }
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