package com.codems.ordertracker.domain.webhook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload sent by the shipping provider for a shipment tracking update")
public record ShipmentWebhookRequest(

        @NotBlank
        @Schema(example = "evt_ship_9182", description = "Unique id of the event, used for idempotency")
        String eventId,

        @NotBlank
        @Schema(example = "shipment.shipped", description = "shipment.shipped or shipment.delivered")
        String eventType,

        @NotBlank
        @Schema(example = "ORD-20260827-4F2A9C31")
        String orderNumber,

        @Schema(example = "TRK-99120031")
        String trackingNumber
) {
}