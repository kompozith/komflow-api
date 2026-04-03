package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Sends a rich-HTML recap email to all configured admin addresses once a
 * campaign finishes (success, partial-success or failure).
 * <p>
 * Recipients are read from {@code app.notifications.admin-emails} as a
 * comma-separated list.  If the property is empty the notification is silently
 * skipped.
 * <p>
 * Any SMTP error is caught-and-logged so that a notification failure can never
 * affect the campaign result or its final status.
 */
@Slf4j
@Service
public class CampaignCompletionNotificationService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private final JavaMailSender mailSender;
    private final String mailFromAddress;
    private final String mailFromName;
    private final String adminEmails;

    public CampaignCompletionNotificationService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String mailFromAddress,
            @Value("${spring.mail.from-name}") String mailFromName,
            @Value("${app.notifications.admin-emails:}") String adminEmails
    ) {
        this.mailSender = mailSender;
        this.mailFromAddress = mailFromAddress;
        this.mailFromName = mailFromName;
        this.adminEmails = adminEmails;
    }

    /**
     * Sends the campaign-completion recap to every configured admin address.
     * This method is intentionally fire-and-forget: it should be called from
     * the async campaign-executor thread and must never throw.
     *
     * @param campaignId    the database id of the campaign
     * @param campaignName  display name of the campaign
     * @param total         total number of contacts processed
     * @param successCount  contacts that received the message successfully
     * @param failureCount  contacts for which the send failed
     * @param finalStatus   final {@link CampaignStatus} of the campaign
     * @param startedAt     moment the execution loop started
     * @param completedAt   moment the execution loop finished
     */
    public void notifyCampaignCompleted(
            Long campaignId,
            String campaignName,
            int total,
            int successCount,
            int failureCount,
            CampaignStatus finalStatus,
            Instant startedAt,
            Instant completedAt
    ) {
        if (!StringUtils.hasText(adminEmails)) {
            log.debug("No admin emails configured – skipping campaign completion notification for campaign {}",
                    campaignId);
            return;
        }

        List<String> recipients = Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        if (recipients.isEmpty()) {
            return;
        }

        try {
            String html    = buildHtml(campaignId, campaignName, total, successCount,
                                       failureCount, finalStatus, startedAt, completedAt);
            String subject = buildSubject(campaignName, finalStatus);

            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFromAddress, mailFromName);
            helper.setTo(recipients.toArray(String[]::new));
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            log.info("Campaign {} completion notification sent to {} admin(s): {}",
                    campaignId, recipients.size(), recipients);
        } catch (Exception e) {
            // MUST NOT re-throw – this is a best-effort notification only
            log.error("Failed to send campaign completion notification for campaign {}: {}",
                    campaignId, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildSubject(String campaignName, CampaignStatus status) {
        String icon = switch (status) {
            case SUCCESS         -> "✅";
            case PARTIAL_SUCCESS -> "⚠️";
            case FAILED          -> "❌";
            default              -> "📋";
        };
        String label = switch (status) {
            case SUCCESS         -> "Succès complet";
            case PARTIAL_SUCCESS -> "Succès partiel";
            case FAILED          -> "Échec";
            default              -> status.name();
        };
        String name = StringUtils.hasText(campaignName) ? campaignName : "Campagne #inconnu";
        return "[Komflow] " + icon + " Campagne terminée — " + name + " — " + label;
    }

    private String buildHtml(
            Long campaignId,
            String campaignName,
            int total,
            int successCount,
            int failureCount,
            CampaignStatus finalStatus,
            Instant startedAt,
            Instant completedAt
    ) {
        Duration duration   = Duration.between(startedAt, completedAt);
        String durationStr  = formatDuration(duration);
        double successRate  = total > 0 ? (successCount * 100.0 / total) : 0.0;

        String statusColor = switch (finalStatus) {
            case SUCCESS         -> "#22c55e";
            case PARTIAL_SUCCESS -> "#f59e0b";
            default              -> "#ef4444";
        };
        String statusLabel = switch (finalStatus) {
            case SUCCESS         -> "✅ Succès complet";
            case PARTIAL_SUCCESS -> "⚠️ Succès partiel";
            case FAILED          -> "❌ Échec";
            default              -> finalStatus.name();
        };

        String name = StringUtils.hasText(campaignName) ? campaignName : "—";

        // Note: %% produces a literal % in String.formatted()
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
                  <title>Rapport de campagne Komflow</title>
                </head>
                <body style="margin:0;padding:0;font-family:'Segoe UI',Helvetica,Arial,sans-serif;background:#f1f5f9;color:#1e293b;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f1f5f9;padding:40px 16px;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:14px;overflow:hidden;
                                    box-shadow:0 4px 32px rgba(0,0,0,.10);max-width:600px;">

                        <!-- ═══ HEADER ═══ -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#1e3a8a 0%%,#2563eb 100%%);
                                     padding:32px 36px 28px;">
                            <p style="margin:0 0 4px;color:#93c5fd;font-size:12px;
                                      letter-spacing:1.5px;text-transform:uppercase;">
                              Komflow · Notification automatique
                            </p>
                            <h1 style="margin:0;color:#ffffff;font-size:24px;
                                       font-weight:700;line-height:1.3;">
                              📊 Rapport de campagne
                            </h1>
                          </td>
                        </tr>

                        <!-- ═══ BODY ═══ -->
                        <tr>
                          <td style="padding:32px 36px;">

                            <!-- Status badge -->
                            <div style="display:inline-block;background:%s;color:#ffffff;
                                        border-radius:24px;padding:7px 20px;font-size:15px;
                                        font-weight:600;margin-bottom:28px;">
                              %s
                            </div>

                            <!-- Campaign meta table -->
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="border-collapse:collapse;margin-bottom:28px;">
                              <tr style="border-bottom:1px solid #f1f5f9;">
                                <td style="padding:10px 0;color:#64748b;font-size:13px;width:170px;">
                                  Campagne
                                </td>
                                <td style="padding:10px 0;font-weight:600;font-size:14px;">
                                  %s
                                </td>
                              </tr>
                              <tr style="border-bottom:1px solid #f1f5f9;">
                                <td style="padding:10px 0;color:#64748b;font-size:13px;">
                                  Identifiant
                                </td>
                                <td style="padding:10px 0;font-size:14px;">
                                  #%d
                                </td>
                              </tr>
                              <tr style="border-bottom:1px solid #f1f5f9;">
                                <td style="padding:10px 0;color:#64748b;font-size:13px;">
                                  Démarrage
                                </td>
                                <td style="padding:10px 0;font-size:14px;">
                                  %s
                                </td>
                              </tr>
                              <tr style="border-bottom:1px solid #f1f5f9;">
                                <td style="padding:10px 0;color:#64748b;font-size:13px;">
                                  Terminée à
                                </td>
                                <td style="padding:10px 0;font-size:14px;">
                                  %s
                                </td>
                              </tr>
                              <tr>
                                <td style="padding:10px 0;color:#64748b;font-size:13px;">
                                  Durée totale
                                </td>
                                <td style="padding:10px 0;font-size:14px;font-weight:500;">
                                  %s
                                </td>
                              </tr>
                            </table>

                            <!-- Stats cards (4 columns) -->
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="border-collapse:separate;border-spacing:0 0;">
                              <tr>
                                <!-- Total -->
                                <td width="25%%" style="padding:0 4px 0 0;">
                                  <div style="background:#f8fafc;border:1px solid #e2e8f0;
                                              border-radius:10px;padding:18px 12px;text-align:center;">
                                    <div style="font-size:30px;font-weight:700;color:#1e293b;">%d</div>
                                    <div style="font-size:11px;color:#64748b;margin-top:5px;
                                                text-transform:uppercase;letter-spacing:.8px;">Total</div>
                                  </div>
                                </td>
                                <!-- Success -->
                                <td width="25%%" style="padding:0 4px;">
                                  <div style="background:#f0fdf4;border:1px solid #bbf7d0;
                                              border-radius:10px;padding:18px 12px;text-align:center;">
                                    <div style="font-size:30px;font-weight:700;color:#16a34a;">%d</div>
                                    <div style="font-size:11px;color:#15803d;margin-top:5px;
                                                text-transform:uppercase;letter-spacing:.8px;">Succès</div>
                                  </div>
                                </td>
                                <!-- Failures -->
                                <td width="25%%" style="padding:0 4px;">
                                  <div style="background:#fff7ed;border:1px solid #fed7aa;
                                              border-radius:10px;padding:18px 12px;text-align:center;">
                                    <div style="font-size:30px;font-weight:700;color:#ea580c;">%d</div>
                                    <div style="font-size:11px;color:#c2410c;margin-top:5px;
                                                text-transform:uppercase;letter-spacing:.8px;">Échecs</div>
                                  </div>
                                </td>
                                <!-- Rate -->
                                <td width="25%%" style="padding:0 0 0 4px;">
                                  <div style="background:#eff6ff;border:1px solid #bfdbfe;
                                              border-radius:10px;padding:18px 12px;text-align:center;">
                                    <div style="font-size:30px;font-weight:700;color:#1d4ed8;">%.1f%%</div>
                                    <div style="font-size:11px;color:#1e40af;margin-top:5px;
                                                text-transform:uppercase;letter-spacing:.8px;">Taux</div>
                                  </div>
                                </td>
                              </tr>
                            </table>

                          </td>
                        </tr>

                        <!-- ═══ FOOTER ═══ -->
                        <tr>
                          <td style="background:#f8fafc;border-top:1px solid #e2e8f0;
                                     padding:16px 36px;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#94a3b8;">
                              Ce message est généré automatiquement par <strong>Komflow</strong>.
                              Merci de ne pas y répondre.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                /* status badge  */ statusColor, statusLabel,
                /* campaign meta */ name, campaignId,
                                   DATE_FMT.format(startedAt),
                                   DATE_FMT.format(completedAt),
                                   durationStr,
                /* stats cards   */ total, successCount, failureCount, successRate
        );
    }

    private String formatDuration(Duration duration) {
        long hours   = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}

