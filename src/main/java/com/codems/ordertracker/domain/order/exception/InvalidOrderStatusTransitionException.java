package com.codems.ordertracker.domain.order.exception;

import com.codems.ordertracker.common.exceptions.types.BaseException;
import com.codems.ordertracker.domain.order.entity.OrderStatus;

public class InvalidOrderStatusTransitionException extends BaseException {

    private InvalidOrderStatusTransitionException(String message) {
        super(OrderErrorType.INVALID_STATUS_TRANSITION, message);
    }

    public static InvalidOrderStatusTransitionException of(OrderStatus current, OrderStatus target) {
        return new InvalidOrderStatusTransitionException(
                "Order cannot move from " + current + " to " + target
        );
    }
}
