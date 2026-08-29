package com.codems.ordertracker.domain.notification.service;

import com.codems.ordertracker.common.config.properties.MailProperties;
import com.codems.ordertracker.domain.order.event.OrderStatusChangedEvent;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Async("mailTaskExecutor")
    @Retryable(
            retryFor = MailException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
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

    @Recover
    public void recoverFromFailedSend(MailException exception, OrderStatusChangedEvent event) {
        log.error(
                "Giving up sending status email for order {} to {} after repeated transient failures",
                event.orderNumber(), event.customerEmail(), exception
        );
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