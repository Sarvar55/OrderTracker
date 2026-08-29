package com.codems.ordertracker.domain.webhook.dto;

import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Audit entry of a received webhook event")
public record WebhookEventResponse(

        Long id,
        WebhookChannel channel,
        String eventType,
        String providerEventId,
        String orderNumber,
        boolean signatureValid,
        WebhookEventStatus status,
        String errorMessage,
        LocalDateTime receivedAt,
        LocalDateTime processedAt
) {
}