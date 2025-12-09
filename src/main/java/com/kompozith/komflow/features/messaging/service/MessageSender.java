package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageSender {

    protected final MessageChannel channel;

    protected MessageSender(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Send a message to a contact via the specific channel
     */
    public void sendMessage(Contact contact, Message message) {
        String personalizedContent = prepareContent(message);
        sendPersonalizedMessage(contact, message, personalizedContent);
    }

    /**
     * Send a personalized message to a contact via the specific channel
     */
    protected abstract void sendPersonalizedMessage(Contact contact, Message message, String personalizedContent);

    /**
     * Validate that the contact has the required information for this channel
     */
    protected abstract boolean validateContact(Contact contact);

    /**
     * Prepare the message content for the specific channel
     */
    protected String prepareContent(Message message) {
        // Default implementation: return body as is
        // Subclasses can override for channel-specific formatting
        return message.getContent();
    }

    /**
     * Log the sending attempt
     */
    protected void logSending(Contact contact, Message message, boolean success, String errorMessage) {
        if (success) {
            log.info("Message sent successfully via {} to contact {} with message {}",
                    channel, contact.getId(), message.getId());
        } else {
            log.error("Failed to send message via {} to contact {}: {}",
                    channel, contact.getId(), errorMessage);
        }
    }

    /**
     * Get the recipient identifier for this channel (email, phone, etc.)
     */
    protected abstract String getRecipientIdentifier(Contact contact);
}