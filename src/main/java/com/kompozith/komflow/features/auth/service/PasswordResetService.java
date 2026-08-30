package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.*;
import com.kompozith.komflow.features.auth.entity.PasswordResetToken;
import com.kompozith.komflow.features.auth.repository.PasswordResetTokenRepository;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int OTP_TTL_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${spring.mail.from-name:Komflow}")
    private String fromName;

    // ─── Étape 1 : initier le reset ──────────────────────────────────────────

    /**
     * Génère un OTP 6 chiffres, l'enregistre en base, et envoie un email.
     * Répond toujours 200 même si le contact est inconnu (sécurité : pas d'énumération).
     * Seul le mode EMAIL est supporté ; PHONE est ignoré silencieusement.
     */
    @Transactional
    public void initiatePasswordReset(PasswordResetInitiateRequest request) {
        // Support EMAIL only for now; PHONE is silently ignored
        if ("PHONE".equalsIgnoreCase(request.contactType())) {
            return;
        }
        String email = request.contact().toLowerCase().trim();
        personRepository.findByEmail(email).ifPresent(person -> {
            userRepository.findByPersonId(person.getId()).ifPresent(user -> {
                tokenRepository.invalidateAllForUser(user.getId());

                String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
                Instant expiresAt = Instant.now().plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES);

                PasswordResetToken token = PasswordResetToken.builder()
                        .user(user)
                        .otpCode(otp)
                        .used(false)
                        .expiresAt(expiresAt)
                        .build();
                tokenRepository.save(token);

                sendOtpEmail(person, otp);
            });
        });
    }

    // ─── Étape 2 : vérifier l'OTP ────────────────────────────────────────────

    @Transactional
    public PasswordResetVerifyResponse verifyOtp(PasswordResetVerifyRequest request) {
        String email = request.contact().toLowerCase().trim();
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Code invalide ou expiré."));

        User user = userRepository.findByPersonId(person.getId())
                .orElseThrow(() -> new IllegalArgumentException("Code invalide ou expiré."));

        PasswordResetToken token = tokenRepository.findByOtpCodeAndUsedFalse(request.otpCode())
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .filter(t -> !t.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("Code invalide ou expiré."));

        // Génèrer le reset token long (64 hex = 256 bits)
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String resetToken = HexFormat.of().formatHex(bytes);

        token.setResetToken(resetToken);
        tokenRepository.save(token);

        return new PasswordResetVerifyResponse(resetToken);
    }

    // ─── Étape 3 : changer le mot de passe ───────────────────────────────────

    @Transactional
    public void completePasswordReset(PasswordResetCompleteRequest request) {
        PasswordResetToken token = tokenRepository.findByResetTokenAndUsedFalse(request.resetToken())
                .filter(t -> !t.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou expiré."));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Password reset completed for user {}", user.getPerson().getEmail());
    }

    // ─── Email ────────────────────────────────────────────────────────────────

    private void sendOtpEmail(Person person, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(person.getEmail());
            helper.setSubject("Code de réinitialisation – Komflow");

            String firstName = person.getFirstName() != null ? person.getFirstName() : "";
            String html = buildOtpEmailHtml(firstName, otp);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("OTP email sent to {}", person.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", person.getEmail(), e.getMessage());
            // On ne lève pas l'exception pour éviter de révéler si l'email existe
        }
    }

    private String buildOtpEmailHtml(String firstName, String otp) {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:20px">
                  <div style="max-width:480px;margin:0 auto;background:#fff;border-radius:8px;padding:32px">
                    <h2 style="color:#2563eb">Komflow</h2>
                    <p>Bonjour%s,</p>
                    <p>Voici votre code de vérification pour réinitialiser votre mot de passe :</p>
                    <div style="font-size:36px;font-weight:bold;letter-spacing:10px;text-align:center;
                                background:#f0f4ff;border-radius:8px;padding:16px;margin:24px 0;color:#2563eb">
                      %s
                    </div>
                    <p style="color:#666">Ce code expire dans <strong>15 minutes</strong>.</p>
                    <p style="color:#999;font-size:12px">Si vous n'avez pas demandé cette réinitialisation,
                       ignorez cet email.</p>
                  </div>
                </body>
                </html>
                """.formatted(
                firstName.isBlank() ? "" : " " + firstName,
                otp
        );
    }
}
