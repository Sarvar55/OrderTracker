package com.codems.ordertracker.domain.webhook.mapper;

import com.codems.ordertracker.domain.webhook.dto.WebhookEventDetailResponse;
import com.codems.ordertracker.domain.webhook.dto.WebhookEventResponse;
import com.codems.ordertracker.domain.webhook.entity.WebhookEvent;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventMapper {

    public WebhookEventResponse toResponse(WebhookEvent event) {
        return new WebhookEventResponse(
                event.getId(),
                event.getChannel(),
                event.getEventType(),
                event.getProviderEventId(),
                event.getOrderNumber(),
                event.isSignatureValid(),
                event.getStatus(),
                event.getErrorMessage(),
                event.getCreatedAt(),
                event.getProcessedAt()
        );
    }

    public WebhookEventDetailResponse toDetailResponse(WebhookEvent event) {
        return new WebhookEventDetailResponse(
                event.getId(),
                event.getChannel(),
                event.getEventType(),
                event.getProviderEventId(),
                event.getOrderNumber(),
                event.getPayload(),
                event.isSignatureValid(),
                event.getStatus(),
                event.getErrorMessage(),
                event.getCreatedAt(),
                event.getProcessedAt()
        );
    }
}