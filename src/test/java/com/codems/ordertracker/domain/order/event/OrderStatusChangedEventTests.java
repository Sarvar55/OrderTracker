package com.codems.ordertracker.domain.order.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.codems.ordertracker.domain.base.event.DomainEvent;
import com.codems.ordertracker.domain.order.entity.Order;
import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderStatusChangedEventTests {

    @Test
    @DisplayName("changing an order status registers a domain event with order and status state")
    void registersStatusChangedEvent() {
        User customer = User.of("sarvar", "sarvar@example.com", "encoded");
        customer.setId(1L);

        Order order = Order.of(customer, "ORD-1001", "USD", "Baku");
        order.setId(10L);

        order.changeStatus(OrderStatus.PAID, "Payment captured", "PAYMENT_WEBHOOK");

        assertThat(order.domainEvents()).hasSize(1);

        DomainEvent domainEvent = order.domainEvents().iterator().next();
        assertThat(domainEvent).isInstanceOf(OrderStatusChangedEvent.class);
        assertThat(domainEvent.eventId()).isNotNull();
        assertThat(domainEvent.occurredAt()).isNotNull();

        OrderStatusChangedEvent event = (OrderStatusChangedEvent) domainEvent;
        assertThat(event.orderId()).isEqualTo(10L);
        assertThat(event.orderNumber()).isEqualTo("ORD-1001");
        assertThat(event.customerEmail()).isEqualTo("sarvar@example.com");
        assertThat(event.previousStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(event.currentStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(event.reason()).isEqualTo("Payment captured");
        assertThat(event.source()).isEqualTo("PAYMENT_WEBHOOK");
    }

    @Test
    @DisplayName("domain events can be cleared after Spring Data publishes them")
    void clearsPublishedEvents() {
        User customer = User.of("sarvar", "sarvar@example.com", "encoded");
        Order order = Order.of(customer, "ORD-1001", "USD", "Baku");

        order.changeStatus(OrderStatus.PAID, "Payment captured", "PAYMENT_WEBHOOK");
        order.clearDomainEvents();

        assertThat(order.domainEvents()).isEmpty();
    }

    @Test
    @DisplayName("setting the initial status does not produce a status changed event")
    void doesNotRegisterEventWhenStatusDidNotChange() {
        User customer = User.of("sarvar", "sarvar@example.com", "encoded");
        Order order = Order.of(customer, "ORD-1001", "USD", "Baku");

        order.changeStatus(OrderStatus.PENDING_PAYMENT, "Order created", "CUSTOMER");

        assertThat(order.domainEvents()).isEmpty();
    }
}
