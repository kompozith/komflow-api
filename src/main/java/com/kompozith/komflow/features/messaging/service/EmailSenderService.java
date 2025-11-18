package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService extends MessageSender implements EmailService {

    private final JavaMailSender mailSender;

    public EmailSenderService(JavaMailSender mailSender) {
        super(MessageChannel.EMAIL);
        this.mailSender = mailSender;
    }

    @Override
    protected void sendPersonalizedMessage(Contact contact, Message message, String personalizedContent) {
        if (!validateContact(contact)) {
            throw new IllegalArgumentException("Contact must have a valid email address");
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(getRecipientIdentifier(contact));
        mailMessage.setSubject(message.getTitle());
        mailMessage.setText(personalizedContent);

        try {
            mailSender.send(mailMessage);
            logSending(contact, message, true, null);
        } catch (Exception e) {
            logSending(contact, message, false, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    protected boolean validateContact(Contact contact) {
        return contact.getPerson() != null && contact.getPerson().getEmail() != null;
    }

    @Override
    protected String getRecipientIdentifier(Contact contact) {
        return contact.getPerson().getEmail();
    }

    // Legacy method for backward compatibility
    @Override
    public void sendEmail(Contact contact, Message message) {
        sendMessage(contact, message);
    }
}