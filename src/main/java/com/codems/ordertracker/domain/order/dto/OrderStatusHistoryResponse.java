package com.codems.ordertracker.domain.order.dto;

import com.codems.ordertracker.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "One entry of the order status audit trail")
public record OrderStatusHistoryResponse(

        @Schema(example = "PENDING_PAYMENT")
        OrderStatus previousStatus,

        @Schema(example = "PAID")
        OrderStatus newStatus,

        @Schema(example = "Payment captured by gateway")
        String reason,

        @Schema(example = "PAYMENT_WEBHOOK")
        String source,

        @Schema(example = "2026-08-27T10:15:30")
        LocalDateTime changedAt
) {
}
