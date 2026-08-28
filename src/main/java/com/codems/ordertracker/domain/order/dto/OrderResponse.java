package com.codems.ordertracker.domain.order.dto;

import com.codems.ordertracker.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Full representation of an order")
public record OrderResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "ORD-20260827-4F2A9C31")
        String orderNumber,

        @Schema(example = "1")
        Long customerId,

        @Schema(example = "PENDING_PAYMENT")
        OrderStatus orderStatus,

        @Schema(example = "299.80")
        BigDecimal totalAmount,

        @Schema(example = "USD")
        String currency,

        @Schema(example = "Baku, Nizami street 12, apt. 5")
        String shippingAddress,

        @Schema(example = "pi_3QkL2mF9x")
        String paymentReference,

        @Schema(example = "TRK-99120031")
        String trackingNumber,

        List<OrderItemResponse> items,

        List<OrderStatusHistoryResponse> statusHistory,

        @Schema(example = "2026-08-27T10:15:30")
        LocalDateTime createdAt,

        @Schema(example = "2026-08-27T11:02:11")
        LocalDateTime updatedAt
) {
}
