package com.codems.ordertracker.domain.order.dto;

import com.codems.ordertracker.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Lightweight order representation used in list responses")
public record OrderSummaryResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "ORD-20260827-4F2A9C31")
        String orderNumber,

        @Schema(example = "PENDING_PAYMENT")
        OrderStatus orderStatus,

        @Schema(example = "299.80")
        BigDecimal totalAmount,

        @Schema(example = "USD")
        String currency,

        @Schema(example = "3")
        int itemCount,

        @Schema(example = "2026-08-27T10:15:30")
        LocalDateTime createdAt
) {
}
