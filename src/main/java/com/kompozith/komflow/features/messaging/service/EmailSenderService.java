package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Service
public class EmailSenderService extends MessageSender implements EmailService {

    private final JavaMailSender mailSender;
    private final String mailFromAddress;
    private final String appName;

    public EmailSenderService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String mailFromAddress,
            @Value("${spring.application.name}") String appName
    ) {
        super(MessageChannel.EMAIL);
        this.mailSender = mailSender;
        this.mailFromAddress = mailFromAddress;
        this.appName = appName;
    }

    @Override
    protected void sendPersonalizedMessage(Contact contact, Message message, String personalizedContent) {
        if (!validateContact(contact)) {
            throw new IllegalArgumentException("Contact must have a valid email address");
        }

        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFromAddress, appName);
            helper.setTo(getRecipientIdentifier(contact));
            helper.setSubject(message.getTitle());
            helper.setText(personalizedContent, true);

            if (message.getAttachments() != null) {
                for (File attachment : message.getAttachments()) {
                    if (attachment == null || !StringUtils.hasText(attachment.getName()) || !StringUtils.hasText(attachment.getUrl())) {
                        continue;
                    }
                    helper.addAttachment(attachment.getName(), new UrlResource(attachment.getUrl()));
                }
            }

            mailSender.send(mimeMessage);
            logSending(contact, message, true, null);
        } catch (Exception e) {
            logSending(contact, message, false, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    protected boolean validateContact(Contact contact) {
        return contact.getPerson() != null && StringUtils.hasText(contact.getPerson().getEmail());
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
