package com.codems.ordertracker.domain.webhook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Schema(description = "Payload sent by the payment gateway for a payment status update")
public record PaymentWebhookRequest(

        @NotBlank
        @Schema(example = "evt_1PxK2mF9x0002", description = "Unique id of the event, used for idempotency")
        String eventId,

        @NotBlank
        @Schema(example = "payment.succeeded", description = "payment.succeeded or payment.failed")
        String eventType,

        @NotBlank
        @Schema(example = "ORD-20260827-4F2A9C31")
        String orderNumber,

        @Schema(example = "pi_3QkL2mF9x")
        String paymentReference,

        @Schema(example = "358.80")
        BigDecimal amount,

        @Schema(example = "Card declined by issuer")
        String failureReason
) {
}