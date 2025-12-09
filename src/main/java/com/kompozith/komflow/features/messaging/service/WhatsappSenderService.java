package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WhatsappSenderService extends MessageSender {

    public WhatsappSenderService() {
        super(MessageChannel.WHATSAPP);
    }

    @Override
    protected void sendPersonalizedMessage(Contact contact, Message message, String personalizedContent) {
        if (!validateContact(contact)) {
            throw new IllegalArgumentException("Contact must have a valid WhatsApp phone number");
        }

        String toNumber = getRecipientIdentifier(contact);

        try {
            // TODO: Integrate with Twilio WhatsApp API
            // For now, this is a stub implementation
            log.info("WhatsApp message would be sent to {}: {}", toNumber, personalizedContent);

            logSending(contact, message, true, null);
            log.info("WhatsApp message sent successfully (stub implementation)");

        } catch (Exception e) {
            logSending(contact, message, false, e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
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
        // WhatsApp messages are typically shorter, but we'll use the body as is
        return message.getContent();
    }
}