package com.codems.ordertracker.domain.notification.listener;

import com.codems.ordertracker.domain.notification.service.MailService;
import com.codems.ordertracker.domain.order.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info(
                "Order status changed eventId={} orderId={} orderNumber={} previousStatus={} currentStatus={} source={}",
                event.eventId(),
                event.orderId(),
                event.orderNumber(),
                event.previousStatus(),
                event.currentStatus(),
                event.source()
        );
        mailService.sendOrderStatusChangedEmail(event);
    }
}
