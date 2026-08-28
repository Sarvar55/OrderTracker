package com.codems.ordertracker.domain.notification.service;

import com.codems.ordertracker.common.config.properties.MailProperties;
import com.codems.ordertracker.domain.notification.event.OrderStatusChangedEvent;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void sendOrderStatusChangedEmail(OrderStatusChangedEvent event) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(mailProperties.fromAddress(), mailProperties.fromName());
            helper.setTo(event.customerEmail());
            helper.setSubject("Order %s status changed".formatted(event.orderNumber()));
            helper.setText(buildBody(event), false);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException exception) {
            throw new IllegalStateException("Could not create order status email", exception);
        }
    }

    private String buildBody(OrderStatusChangedEvent event) {
        return """
                Hello,

                The status of your order %s changed from %s to %s.

                Thank you for shopping with us.
                """.formatted(
                event.orderNumber(),
                event.previousStatus(),
                event.currentStatus()
        );
    }
}
