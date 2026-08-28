package com.codems.ordertracker.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Single line item of an order")
public record OrderItemRequest(

        @Schema(example = "Mechanical keyboard")
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must be at most 255 characters")
        String productName,

        @Schema(example = "KB-8721")
        @NotBlank(message = "Product SKU is required")
        @Size(max = 100, message = "Product SKU must be at most 100 characters")
        String productSku,

        @Schema(example = "2")
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Schema(example = "149.90")
        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "Unit price must have at most 2 decimal places")
        BigDecimal unitPrice
) {
}
