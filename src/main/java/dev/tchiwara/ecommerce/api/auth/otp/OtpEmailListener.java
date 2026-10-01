package dev.tchiwara.ecommerce.api.auth.otp;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpEmailListener {

    private final JavaMailSender mailSender;
    private final OtpConfig otpConfig;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    @EventListener
    public void onOtpIssued(OtpIssuedEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(event.email());
            helper.setSubject("Your verification code");
            helper.setText(plainText(event), htmlBody(event));
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}", event.email(), ex);
        }
    }

    private String plainText(OtpIssuedEvent event) {
        return "Hi " + event.name() + ",\n\n"
                + "Your verification code is " + event.code() + ".\n"
                + "It expires in " + otpConfig.getTtlMinutes() + " minutes.\n\n"
                + "If you didn't create an account, you can ignore this email.";
    }

    private String htmlBody(OtpIssuedEvent event) {
        return """
                <div style="font-family: Arial, Helvetica, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px; color: #222;">
                  <p>Hi <strong>%s</strong>,</p>
                  <p><strong>Your verification code is:</strong></p>
                  <p style="font-size: 36px; font-weight: bold; letter-spacing: 8px; text-align: center; margin: 24px 0;">%s</p>
                  <p>It expires in %d minutes.</p>
                  <p style="color: #666; font-size: 13px;">If you didn't create an account, you can ignore this email.</p>
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(event.name()),
                event.code(),
                otpConfig.getTtlMinutes());
    }
}