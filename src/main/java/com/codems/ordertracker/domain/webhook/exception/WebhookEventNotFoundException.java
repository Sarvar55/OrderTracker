package com.codems.ordertracker.domain.webhook.exception;

import com.codems.ordertracker.common.exceptions.types.BaseException;

public class WebhookEventNotFoundException extends BaseException {

    private WebhookEventNotFoundException(String message) {
        super(WebhookErrorType.WEBHOOK_EVENT_NOT_FOUND, message);
    }

    public static WebhookEventNotFoundException byId(Long id) {
        return new WebhookEventNotFoundException("Webhook event not found: " + id);
    }
}