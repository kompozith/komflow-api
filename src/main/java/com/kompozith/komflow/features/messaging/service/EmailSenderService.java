package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.core.service.FileStorageService;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailSenderService extends MessageSender implements EmailService {

    /**
     * Delays (ms) between successive AUTH-failure retries: 10 s, then 20 s.
     * A short back-off gives Gmail time to lift a temporary rate-limit block
     * without stalling the campaign for too long.
     */
    private static final long[] RETRY_DELAY_MS = {10_000L, 20_000L};

    private final JavaMailSender mailSender;
    private final FileStorageService fileStorageService;
    private final String mailFromAddress;
    private final String mailFromName;

    /**
     * Maximum number of send attempts per email (1 attempt + retries).
     * Configurable via {@code app.campaign.email-max-retry-attempts}.
     */
    @Value("${app.campaign.email-max-retry-attempts:2}")
    private int maxRetryAttempts;

    public EmailSenderService(
            JavaMailSender mailSender,
            FileStorageService fileStorageService,
            @Value("${spring.mail.username}") String mailFromAddress,
            @Value("${spring.mail.from-name}") String mailFromName
    ) {
        super(MessageChannel.EMAIL);
        this.mailSender = mailSender;
        this.fileStorageService = fileStorageService;
        this.mailFromAddress = mailFromAddress;
        this.mailFromName = mailFromName;
    }

    @Override
    protected void sendPersonalizedMessage(Contact contact, Message message, String personalizedContent) {
        if (!validateContact(contact)) {
            throw new IllegalArgumentException("Contact must have a valid email address");
        }

        Exception lastAuthException = null;

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                doSendEmail(contact, message, personalizedContent);
                logSending(contact, message, true, null);
                return; // success
            } catch (MailAuthenticationException e) {
                lastAuthException = e;
                long delayMs = RETRY_DELAY_MS[Math.min(attempt - 1, RETRY_DELAY_MS.length - 1)];
                log.warn("SMTP authentication failure for contact {} (attempt {}/{}). "
                                + "Waiting {}ms before retry...",
                        contact.getId(), attempt, maxRetryAttempts, delayMs);
                if (attempt < maxRetryAttempts) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                // Non-auth failures (network, template, etc.) – no retry benefit
                logSending(contact, message, false, e.getMessage());
                throw new RuntimeException("Failed to send email", e);
            }
        }

        // All attempts exhausted due to auth failures
        String errorMsg = lastAuthException != null ? lastAuthException.getMessage() : "Authentication failed";
        logSending(contact, message, false,
                "Auth failed after " + maxRetryAttempts + " attempt(s): " + errorMsg);
        throw new RuntimeException("Failed to send email", lastAuthException);
    }

    /**
     * Performs the actual MIME message construction and SMTP transmission.
     * Throws {@link MailAuthenticationException} when Gmail rejects credentials,
     * and {@link jakarta.mail.MessagingException} for low-level MIME errors.
     */
    private void doSendEmail(Contact contact, Message message, String personalizedContent) throws Exception {
        var mimeMessage = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        helper.setFrom(mailFromAddress, mailFromName);
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
