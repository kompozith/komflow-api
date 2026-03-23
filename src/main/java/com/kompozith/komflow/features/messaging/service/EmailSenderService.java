package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.core.service.FileStorageService;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Service
public class EmailSenderService extends MessageSender implements EmailService {

    private final JavaMailSender mailSender;
    private final FileStorageService fileStorageService;
    private final String mailFromAddress;
    private final String appName;

    public EmailSenderService(
            JavaMailSender mailSender,
            FileStorageService fileStorageService,
            @Value("${spring.mail.username}") String mailFromAddress,
            @Value("${spring.application.name}") String appName
    ) {
        super(MessageChannel.EMAIL);
        this.mailSender = mailSender;
        this.fileStorageService = fileStorageService;
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
                    if (attachment == null || attachment.getId() == null || !StringUtils.hasText(attachment.getName())) {
                        continue;
                    }

                    Resource resource = fileStorageService.loadAsResource(attachment.getId());
                    String fileName = attachment.getName();
                    MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM);
                    helper.addAttachment(fileName, resource, mediaType.toString());
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
