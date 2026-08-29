package com.codems.ordertracker.domain.webhook.exception;

import com.codems.ordertracker.common.exceptions.types.BaseException;

public class InvalidWebhookSignatureException extends BaseException {

    private InvalidWebhookSignatureException(String message) {
        super(WebhookErrorType.INVALID_SIGNATURE, message);
    }

    public static InvalidWebhookSignatureException of() {
        return new InvalidWebhookSignatureException(WebhookErrorType.INVALID_SIGNATURE.message());
    }
}