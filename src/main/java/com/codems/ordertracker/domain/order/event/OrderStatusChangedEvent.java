package com.codems.ordertracker.domain.order.event;

import com.codems.ordertracker.domain.order.entity.OrderStatus;
import java.util.Objects;

public final class OrderStatusChangedEvent extends OrderEvent {

    private final OrderStatus previousStatus;
    private final OrderStatus currentStatus;
    private final String reason;
    private final String source;

    public OrderStatusChangedEvent(
            Long orderId,
            String orderNumber,
            String customerEmail,
            OrderStatus previousStatus,
            OrderStatus currentStatus,
            String reason,
            String source
    ) {
        super(orderId, orderNumber, customerEmail);
        this.previousStatus = Objects.requireNonNull(previousStatus, "previousStatus must not be null");
        this.currentStatus = Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        this.reason = reason;
        this.source = Objects.requireNonNull(source, "source must not be null");
    }

    public OrderStatus previousStatus() {
        return previousStatus;
    }

    public OrderStatus currentStatus() {
        return currentStatus;
    }

    public String reason() {
        return reason;
    }

    public String source() {
        return source;
    }
}
