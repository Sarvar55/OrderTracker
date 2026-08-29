package com.codems.ordertracker.domain.webhook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.order.exception.OrderNotFoundException;
import com.codems.ordertracker.domain.order.service.OrderService;
import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEvent;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import com.codems.ordertracker.domain.webhook.exception.InvalidWebhookSignatureException;
import com.codems.ordertracker.domain.webhook.repository.WebhookEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebhookServiceTests {

    private static final String ORDER_NUMBER = "ORD-20260827-4F2A9C31";
    private static final String SIGNATURE = "sha256=validsignature";

    @Mock
    private WebhookSignatureVerifier signatureVerifier;

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @Mock
    private OrderService orderService;

    private WebhookService webhookService;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(signatureVerifier, webhookEventRepository, orderService, new ObjectMapper());
        when(signatureVerifier.isValid(any(), any())).thenReturn(true);
        when(webhookEventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("rejects a payment webhook with an invalid signature")
    void rejectsInvalidSignature() {
        when(signatureVerifier.isValid(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> webhookService.processPaymentWebhook(paymentPayload("payment.succeeded"), SIGNATURE))
                .isInstanceOf(InvalidWebhookSignatureException.class);

        verify(webhookEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("payment.succeeded marks the order as PAID and stores a PROCESSED event")
    void processesSuccessfulPayment() {
        webhookService.processPaymentWebhook(paymentPayload("payment.succeeded"), SIGNATURE);

        verify(orderService).attachPaymentReference(ORDER_NUMBER, "pi_3QkL2mF9x");
        verify(orderService).applyExternalStatusChange(
                eq(ORDER_NUMBER), eq(OrderStatus.PAID), any(), eq(WebhookService.PAYMENT_SOURCE));

        ArgumentCaptor<WebhookEvent> captor = ArgumentCaptor.forClass(WebhookEvent.class);
        verify(webhookEventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
    }

    @Test
    @DisplayName("duplicate webhook events are ignored without touching the order")
    void ignoresDuplicateEvent() {
        when(webhookEventRepository.existsByChannelAndProviderEventId(WebhookChannel.PAYMENT, "evt_1"))
                .thenReturn(true);

        webhookService.processPaymentWebhook(paymentPayload("payment.succeeded"), SIGNATURE);

        verify(orderService, never()).applyExternalStatusChange(any(), any(), any(), any());
        verify(webhookEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("an order that no longer exists results in a FAILED event, not an exception")
    void marksEventFailedWhenOrderMissing() {
        when(orderService.applyExternalStatusChange(any(), any(), any(), any()))
                .thenThrow(OrderNotFoundException.byOrderNumber(ORDER_NUMBER));

        webhookService.processPaymentWebhook(paymentPayload("payment.failed"), SIGNATURE);

        ArgumentCaptor<WebhookEvent> captor = ArgumentCaptor.forClass(WebhookEvent.class);
        verify(webhookEventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(WebhookEventStatus.FAILED);
    }

    @Test
    @DisplayName("an unsupported event type is stored as IGNORED")
    void ignoresUnsupportedEventType() {
        webhookService.processPaymentWebhook(paymentPayload("payment.refunded"), SIGNATURE);

        ArgumentCaptor<WebhookEvent> captor = ArgumentCaptor.forClass(WebhookEvent.class);
        verify(webhookEventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(WebhookEventStatus.IGNORED);
    }

    private String paymentPayload(String eventType) {
        return """
                {
                  "eventId": "evt_1",
                  "eventType": "%s",
                  "orderNumber": "%s",
                  "paymentReference": "pi_3QkL2mF9x",
                  "amount": 358.80
                }
                """.formatted(eventType, ORDER_NUMBER);
    }
}