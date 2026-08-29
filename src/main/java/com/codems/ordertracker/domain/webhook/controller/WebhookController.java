package com.codems.ordertracker.domain.webhook.controller;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import com.codems.ordertracker.domain.webhook.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Receives real-time event notifications from external payment and shipping providers")
public class WebhookController {

    public static final String SIGNATURE_HEADER = "X-Webhook-Signature";

    private final WebhookService webhookService;

    @PostMapping(value = "/payment", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Receive a payment status webhook",
            description = "Called by the payment gateway to notify of a payment success or failure. "
                    + "The raw request body must be signed with the shared webhook secret."
    )
    public ResponseEntity<Void> payment(
            @RequestBody String payload,
            @Parameter(description = "HMAC-SHA256 signature of the raw body, e.g. sha256=<hex>")
            @RequestHeader(SIGNATURE_HEADER) String signature
    ) {
        webhookService.processPaymentWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/shipment", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Receive a shipment tracking webhook",
            description = "Called by the shipping provider to notify of a shipped or delivered package. "
                    + "The raw request body must be signed with the shared webhook secret."
    )
    public ResponseEntity<Void> shipment(
            @RequestBody String payload,
            @Parameter(description = "HMAC-SHA256 signature of the raw body, e.g. sha256=<hex>")
            @RequestHeader(SIGNATURE_HEADER) String signature
    ) {
        webhookService.processShipmentWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}