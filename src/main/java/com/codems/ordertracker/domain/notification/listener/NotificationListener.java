package com.codems.ordertracker.domain.notification.listener;

import com.codems.ordertracker.domain.notification.service.MailService;
import com.codems.ordertracker.domain.order.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        mailService.sendOrderStatusChangedEmail(event);
    }
}
