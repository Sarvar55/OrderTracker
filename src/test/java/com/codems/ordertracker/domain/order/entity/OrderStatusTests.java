package com.codems.ordertracker.domain.order.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderStatusTests {

    @Test
    @DisplayName("payment webhook can move a pending order to PAID or PAYMENT_FAILED")
    void allowsPaymentOutcomes() {
        assertThat(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.PAYMENT_FAILED)).isTrue();
    }

    @Test
    @DisplayName("an order cannot be shipped before it is paid")
    void rejectsShippingUnpaidOrder() {
        assertThat(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
    }

    @Test
    @DisplayName("delivered and cancelled are final states")
    void finalStates() {
        assertThat(OrderStatus.DELIVERED.isFinal()).isTrue();
        assertThat(OrderStatus.CANCELLED.isFinal()).isTrue();
        assertThat(OrderStatus.PAID.isFinal()).isFalse();
    }

    @Test
    @DisplayName("only orders that are not shipped yet can be cancelled")
    void cancellableStates() {
        assertThat(OrderStatus.PENDING_PAYMENT.isCancellable()).isTrue();
        assertThat(OrderStatus.PAID.isCancellable()).isTrue();
        assertThat(OrderStatus.SHIPPED.isCancellable()).isFalse();
        assertThat(OrderStatus.DELIVERED.isCancellable()).isFalse();
    }
}
