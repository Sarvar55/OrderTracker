package com.codems.ordertracker.domain.order.entity;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {

	PENDING_PAYMENT,
	PAID,
	PAYMENT_FAILED,
	SHIPPED,
	DELIVERED,
	CANCELLED;

	private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
			PENDING_PAYMENT, EnumSet.of(PAID, PAYMENT_FAILED, CANCELLED),
			PAID, EnumSet.of(SHIPPED, CANCELLED),
			PAYMENT_FAILED, EnumSet.of(PENDING_PAYMENT, CANCELLED),
			SHIPPED, EnumSet.of(DELIVERED),
			DELIVERED, EnumSet.noneOf(OrderStatus.class),
			CANCELLED, EnumSet.noneOf(OrderStatus.class)
	);

	public boolean canTransitionTo(OrderStatus target) {
		return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(target);
	}

	public boolean isFinal() {
		return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).isEmpty();
	}

	public boolean isCancellable() {
		return canTransitionTo(CANCELLED);
	}
}
