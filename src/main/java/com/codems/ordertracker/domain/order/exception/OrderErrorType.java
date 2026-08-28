package com.codems.ordertracker.domain.order.exception;

import com.codems.ordertracker.common.exceptions.types.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorType implements ErrorType {

    ORDER_NOT_FOUND("ORDER_001", "Order not found", HttpStatus.NOT_FOUND),
    INVALID_STATUS_TRANSITION("ORDER_002", "Order status transition is not allowed", HttpStatus.CONFLICT),
    ORDER_NOT_MODIFIABLE("ORDER_003", "Order can no longer be modified", HttpStatus.CONFLICT),
    ORDER_ACCESS_DENIED("ORDER_004", "You are not allowed to access this order", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
