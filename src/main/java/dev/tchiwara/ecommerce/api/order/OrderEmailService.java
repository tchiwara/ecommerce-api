package dev.tchiwara.ecommerce.api.order;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEmailService {

    private final JavaMailSender mailSender;
    private final OrderEmailBuilder emailBuilder;

    @Value("${spring.mail.username}")
    private String from;

    public void sendConfirmationEmail(Order order) throws MessagingException {
        if (order.getConfirmationEmailSentAt() != null) {
            log.info(
                    "Order confirmation email already sent: orderId={}",
                    order.getId()
            );
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(emailBuilder.subject(order));
            helper.setText(
                    emailBuilder.plainText(order),
                    emailBuilder.html(order)
            );

            mailSender.send(message);

            order.markConfirmationEmailSent();

            log.info(
                    "Order confirmation email sent: orderId={}",
                    order.getId()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to send order confirmation email: orderId={}",
                    order.getId(),
                    exception
            );

            throw exception;
        }
    }
}