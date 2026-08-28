package com.codems.ordertracker.domain.notification.listener;

import com.codems.ordertracker.domain.notification.event.OrderStatusChangedEvent;
import com.codems.ordertracker.domain.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final MailService mailService;

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        mailService.sendOrderStatusChangedEmail(event);
    }
}
