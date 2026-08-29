package com.codems.ordertracker.domain.webhook.exception;

import com.codems.ordertracker.common.exceptions.types.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WebhookErrorType implements ErrorType {

    INVALID_SIGNATURE("WEBHOOK_001", "Webhook signature is missing or invalid", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_EVENT_TYPE("WEBHOOK_002", "Webhook event type is not supported", HttpStatus.UNPROCESSABLE_ENTITY),
    WEBHOOK_EVENT_NOT_FOUND("WEBHOOK_003", "Webhook event not found", HttpStatus.NOT_FOUND);

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