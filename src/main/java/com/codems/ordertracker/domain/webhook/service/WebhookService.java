package com.codems.ordertracker.domain.webhook.service;

import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.order.exception.OrderNotFoundException;
import com.codems.ordertracker.domain.order.service.OrderService;
import com.codems.ordertracker.domain.webhook.dto.PaymentWebhookRequest;
import com.codems.ordertracker.domain.webhook.dto.ShipmentWebhookRequest;
import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEvent;
import com.codems.ordertracker.domain.webhook.exception.InvalidWebhookSignatureException;
import com.codems.ordertracker.domain.webhook.repository.WebhookEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    public static final String PAYMENT_SOURCE = "PAYMENT_WEBHOOK";
    public static final String SHIPMENT_SOURCE = "SHIPMENT_WEBHOOK";

    private static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    private static final String PAYMENT_FAILED = "payment.failed";
    private static final String SHIPMENT_SHIPPED = "shipment.shipped";
    private static final String SHIPMENT_DELIVERED = "shipment.delivered";

    private final WebhookSignatureVerifier signatureVerifier;
    private final WebhookEventRepository webhookEventRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processPaymentWebhook(String rawPayload, String signatureHeader) {
        if (!signatureVerifier.isValid(rawPayload, signatureHeader)) {
            log.warn("Rejected payment webhook: invalid signature");
            throw InvalidWebhookSignatureException.of();
        }

        PaymentWebhookRequest request = parse(rawPayload, PaymentWebhookRequest.class);
        if (isDuplicate(WebhookChannel.PAYMENT, request.eventId())) {
            return;
        }

        WebhookEvent event = WebhookEvent.received(
                WebhookChannel.PAYMENT, request.eventType(), request.eventId(),
                request.orderNumber(), rawPayload, true);

        switch (request.eventType()) {
            case PAYMENT_SUCCEEDED -> process(event, () -> {
                if (request.paymentReference() != null) {
                    orderService.attachPaymentReference(request.orderNumber(), request.paymentReference());
                }
                orderService.applyExternalStatusChange(
                        request.orderNumber(), OrderStatus.PAID, "Payment captured by gateway", PAYMENT_SOURCE);
            });
            case PAYMENT_FAILED -> process(event, () -> orderService.applyExternalStatusChange(
                    request.orderNumber(), OrderStatus.PAYMENT_FAILED,
                    request.failureReason() == null ? "Payment declined by gateway" : request.failureReason(),
                    PAYMENT_SOURCE));
            default -> event.markIgnored("Unsupported payment event type: " + request.eventType());
        }

        webhookEventRepository.save(event);
    }

    @Transactional
    public void processShipmentWebhook(String rawPayload, String signatureHeader) {
        if (!signatureVerifier.isValid(rawPayload, signatureHeader)) {
            log.warn("Rejected shipment webhook: invalid signature");
            throw InvalidWebhookSignatureException.of();
        }

        ShipmentWebhookRequest request = parse(rawPayload, ShipmentWebhookRequest.class);
        if (isDuplicate(WebhookChannel.SHIPMENT, request.eventId())) {
            return;
        }

        WebhookEvent event = WebhookEvent.received(
                WebhookChannel.SHIPMENT, request.eventType(), request.eventId(),
                request.orderNumber(), rawPayload, true);

        switch (request.eventType()) {
            case SHIPMENT_SHIPPED -> process(event, () -> {
                if (request.trackingNumber() != null) {
                    orderService.attachTrackingNumber(request.orderNumber(), request.trackingNumber());
                }
                orderService.applyExternalStatusChange(
                        request.orderNumber(), OrderStatus.SHIPPED, "Package handed to carrier", SHIPMENT_SOURCE);
            });
            case SHIPMENT_DELIVERED -> process(event, () -> orderService.applyExternalStatusChange(
                    request.orderNumber(), OrderStatus.DELIVERED, "Package delivered", SHIPMENT_SOURCE));
            default -> event.markIgnored("Unsupported shipment event type: " + request.eventType());
        }

        webhookEventRepository.save(event);
    }

    private void process(WebhookEvent event, Runnable action) {
        try {
            action.run();
            event.markProcessed();
        } catch (OrderNotFoundException exception) {
            log.warn("Webhook event {} references unknown order {}",
                    event.getProviderEventId(), event.getOrderNumber());
            event.markFailed(exception.getMessage());
        } catch (RuntimeException exception) {
            log.error("Failed to process webhook event {}", event.getProviderEventId(), exception);
            event.markFailed(exception.getMessage());
        }
    }

    private boolean isDuplicate(WebhookChannel channel, String providerEventId) {
        boolean duplicate = webhookEventRepository.existsByChannelAndProviderEventId(channel, providerEventId);
        if (duplicate) {
            log.info("Ignoring duplicate {} webhook event {}", channel, providerEventId);
        }
        return duplicate;
    }

    private <T> T parse(String rawPayload, Class<T> type) {
        try {
            return objectMapper.readValue(rawPayload, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Malformed webhook payload", exception);
        }
    }
}