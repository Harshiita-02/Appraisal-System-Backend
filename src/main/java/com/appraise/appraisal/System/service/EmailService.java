package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.entity.enums.NotificationType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Removed @Async — sending synchronously so we can see errors clearly.
 * Once emails are confirmed working we can add @Async back.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendNotificationEmail(
            String toEmail,
            String toName,
            String title,
            String message,
            NotificationType type
    ) {
        log.info("=== EMAIL ATTEMPT: from={} to={} subject={}", fromAddress, toEmail, title);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress, "Appraisal System");
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(buildHtml(toName, title, message, type), true);

            mailSender.send(mime);
            log.info("=== EMAIL SUCCESS: to={} subject={}", toEmail, title);

        } catch (Exception e) {
            log.error("=== EMAIL FAILED: to={} subject={} error={}", toEmail, title, e.getMessage(), e);
        }
    }

    private String accentColor(NotificationType type) {
        return switch (type) {
            case SUCCESS   -> "#16a34a";
            case WARNING   -> "#d97706";
            case APPRAISAL -> "#7c3aed";
            case REVIEW    -> "#2563eb";
            case GOAL      -> "#0891b2";
            default        -> "#6b7280";
        };
    }

    private String typeLabel(NotificationType type) {
        return switch (type) {
            case SUCCESS   -> "Success";
            case WARNING   -> "Warning";
            case APPRAISAL -> "Appraisal";
            case REVIEW    -> "Review";
            case GOAL      -> "Goal";
            default        -> "Info";
        };
    }

    private String buildHtml(String toName, String title, String message, NotificationType type) {
        String accent = accentColor(type);
        String label  = typeLabel(type);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,Helvetica,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f3f4f6;padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:12px;overflow:hidden;
                                      box-shadow:0 1px 3px rgba(0,0,0,.1);">
                          <tr>
                            <td style="background:%s;padding:20px 32px;">
                              <span style="color:#ffffff;font-size:13px;font-weight:600;
                                           letter-spacing:.5px;text-transform:uppercase;">
                                Appraisal System &nbsp;·&nbsp; %s
                              </span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px;">
                              <p style="margin:0 0 8px;font-size:13px;color:#6b7280;">Hi %s,</p>
                              <h1 style="margin:0 0 16px;font-size:20px;font-weight:700;color:#111827;">%s</h1>
                              <p style="margin:0 0 24px;font-size:15px;line-height:1.6;color:#374151;">%s</p>
                              <hr style="border:none;border-top:1px solid #e5e7eb;margin:0 0 24px;"/>
                              <p style="margin:0;font-size:12px;color:#9ca3af;line-height:1.5;">
                                This is an automated notification from your Appraisal System.<br/>
                                Please do not reply to this email.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#f9fafb;padding:16px 32px;border-top:1px solid #e5e7eb;">
                              <p style="margin:0;font-size:12px;color:#9ca3af;text-align:center;">
                                © 2024 Appraisal System. All rights reserved.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(title, accent, label, toName, title, message);
    }
}