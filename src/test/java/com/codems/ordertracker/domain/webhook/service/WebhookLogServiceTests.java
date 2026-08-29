package com.codems.ordertracker.domain.webhook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codems.ordertracker.domain.webhook.dto.WebhookEventDetailResponse;
import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEvent;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import com.codems.ordertracker.domain.webhook.exception.WebhookEventNotFoundException;
import com.codems.ordertracker.domain.webhook.mapper.WebhookEventMapper;
import com.codems.ordertracker.domain.webhook.repository.WebhookEventRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookLogServiceTests {

    @Mock
    private WebhookEventRepository webhookEventRepository;

    private WebhookLogService webhookLogService;

    @BeforeEach
    void setUp() {
        webhookLogService = new WebhookLogService(webhookEventRepository, new WebhookEventMapper());
    }

    @Test
    @DisplayName("returns the detail of an existing webhook event")
    void returnsWebhookEventDetail() {
        WebhookEvent event = WebhookEvent.received(
                WebhookChannel.SHIPMENT, "shipment.delivered", "evt_9", "ORD-1", "{}", true);
        event.setId(42L);
        event.markProcessed();

        when(webhookEventRepository.findById(42L)).thenReturn(Optional.of(event));

        WebhookEventDetailResponse response = webhookLogService.getById(42L);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo(WebhookEventStatus.PROCESSED);
        assertThat(response.channel()).isEqualTo(WebhookChannel.SHIPMENT);
    }

    @Test
    @DisplayName("throws when the webhook event does not exist")
    void throwsWhenEventMissing() {
        when(webhookEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> webhookLogService.getById(99L))
                .isInstanceOf(WebhookEventNotFoundException.class);
    }
}