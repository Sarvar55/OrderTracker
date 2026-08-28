package com.codems.ordertracker.domain.order.exception;

import com.codems.ordertracker.common.exceptions.types.BaseException;

public class OrderNotFoundException extends BaseException {

    private OrderNotFoundException(String message) {
        super(OrderErrorType.ORDER_NOT_FOUND, message);
    }

    public static OrderNotFoundException byId(Long id) {
        return new OrderNotFoundException("Order not found: " + id);
    }

    public static OrderNotFoundException byOrderNumber(String orderNumber) {
        return new OrderNotFoundException("Order not found: " + orderNumber);
    }
}
