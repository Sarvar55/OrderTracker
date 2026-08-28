package com.codems.ordertracker.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Single line item of an order")
public record OrderItemResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Mechanical keyboard")
        String productName,

        @Schema(example = "KB-8721")
        String productSku,

        @Schema(example = "2")
        Integer quantity,

        @Schema(example = "149.90")
        BigDecimal unitPrice,

        @Schema(example = "299.80")
        BigDecimal lineTotal
) {
}
