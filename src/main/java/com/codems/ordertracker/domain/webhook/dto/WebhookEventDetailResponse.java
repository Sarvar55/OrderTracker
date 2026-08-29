package com.codems.ordertracker.domain.webhook.dto;

import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Full audit entry of a received webhook event, including the raw payload")
public record WebhookEventDetailResponse(

        Long id,
        WebhookChannel channel,
        String eventType,
        String providerEventId,
        String orderNumber,
        String payload,
        boolean signatureValid,
        WebhookEventStatus status,
        String errorMessage,
        LocalDateTime receivedAt,
        LocalDateTime processedAt
) {
}