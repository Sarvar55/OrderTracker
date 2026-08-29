package com.codems.ordertracker.domain.webhook.entity;

import com.codems.ordertracker.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "webhook_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class WebhookEvent extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WebhookChannel channel;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "provider_event_id", nullable = false, length = 150)
    private String providerEventId;

    @Column(name = "order_number", length = 40)
    private String orderNumber;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(name = "signature_valid", nullable = false)
    private boolean signatureValid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WebhookEventStatus status = WebhookEventStatus.RECEIVED;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public static WebhookEvent received(
            WebhookChannel channel,
            String eventType,
            String providerEventId,
            String orderNumber,
            String payload,
            boolean signatureValid
    ) {
        WebhookEvent event = new WebhookEvent();
        event.setChannel(channel);
        event.setEventType(eventType);
        event.setProviderEventId(providerEventId);
        event.setOrderNumber(orderNumber);
        event.setPayload(payload);
        event.setSignatureValid(signatureValid);
        event.setStatus(WebhookEventStatus.RECEIVED);
        return event;
    }

    public void markProcessed() {
        this.status = WebhookEventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = WebhookEventStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void markIgnored(String reason) {
        this.status = WebhookEventStatus.IGNORED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = reason;
    }
}