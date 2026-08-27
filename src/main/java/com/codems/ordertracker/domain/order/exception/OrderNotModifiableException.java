package com.codems.ordertracker.domain.order.exception;

import com.codems.ordertracker.common.exceptions.types.BaseException;
import com.codems.ordertracker.domain.order.entity.OrderStatus;

public class OrderNotModifiableException extends BaseException {

    private OrderNotModifiableException(String message) {
        super(OrderErrorType.ORDER_NOT_MODIFIABLE, message);
    }

    public static OrderNotModifiableException of(OrderStatus current) {
        return new OrderNotModifiableException(
                "Order can only be modified while it is awaiting payment, current status: " + current
        );
    }
}
