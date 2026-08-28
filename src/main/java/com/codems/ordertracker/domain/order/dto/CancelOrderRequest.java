package com.codems.ordertracker.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional explanation stored in the order status audit trail")
public record CancelOrderRequest(

        @Schema(example = "Ordered the wrong size")
        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {
}
