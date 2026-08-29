package com.codems.ordertracker.domain.order.event;

import com.codems.ordertracker.domain.base.event.AbstractDomainEvent;
import java.util.Objects;

public abstract class OrderEvent extends AbstractDomainEvent {

    private final Long orderId;
    private final String orderNumber;
    private final String customerEmail;

    protected OrderEvent(Long orderId, String orderNumber, String customerEmail) {
        this.orderId = orderId;
        this.orderNumber = Objects.requireNonNull(orderNumber, "orderNumber must not be null");
        this.customerEmail = Objects.requireNonNull(customerEmail, "customerEmail must not be null");
    }

    public final Long orderId() {
        return orderId;
    }

    public final String orderNumber() {
        return orderNumber;
    }

    public final String customerEmail() {
        return customerEmail;
    }
}
