package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsSenderService extends MessageSender {

    public SmsSenderService() {
        super(MessageChannel.SMS);
    }

    @Override
    protected void sendPersonalizedMessage(Contact contact, Message message, String personalizedContent) {
        if (!validateContact(contact)) {
            throw new IllegalArgumentException("Contact must have a valid phone number");
        }

        String toNumber = getRecipientIdentifier(contact);

        try {
            // TODO: Integrate with Twilio SMS API
            // For now, this is a stub implementation
            log.info("SMS would be sent to {}: {}", toNumber, personalizedContent);

            logSending(contact, message, true, null);
            log.info("SMS sent successfully (stub implementation)");

        } catch (Exception e) {
            logSending(contact, message, false, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    @Override
    protected boolean validateContact(Contact contact) {
        if (contact.getPerson() == null || contact.getPerson().getPhoneNumbers() == null) {
            return false;
        }

        return contact.getPerson().getPhoneNumbers().stream()
                .anyMatch(phone -> phone.getNumber() != null);
    }

    @Override
    protected String getRecipientIdentifier(Contact contact) {
        return contact.getPerson().getPhoneNumbers().stream()
                .filter(phone -> phone.getNumber() != null)
                .findFirst()
                .map(phone -> phone.getNumber())
                .orElse(null);
    }

    @Override
    protected String prepareContent(Message message) {
        // SMS messages have length limits, truncate if necessary
        String content = message.getContent();
        if (content.length() > 160) {
            content = content.substring(0, 157) + "...";
            log.warn("SMS content truncated to fit length limit");
        }
        return content;
    }
}