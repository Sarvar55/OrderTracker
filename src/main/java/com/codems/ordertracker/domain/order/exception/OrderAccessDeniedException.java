package com.codems.ordertracker.domain.order.exception;

import com.codems.ordertracker.common.exceptions.types.BaseException;

public class OrderAccessDeniedException extends BaseException {

    private OrderAccessDeniedException(String message) {
        super(OrderErrorType.ORDER_ACCESS_DENIED, message);
    }

    public static OrderAccessDeniedException of() {
        return new OrderAccessDeniedException(OrderErrorType.ORDER_ACCESS_DENIED.message());
    }
}
