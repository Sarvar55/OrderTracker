package com.codems.ordertracker.domain.notification.event;

public record OrderStatusChangedEvent(
        String orderNumber,
        String customerEmail,
        String previousStatus,
        String currentStatus
) {
}
