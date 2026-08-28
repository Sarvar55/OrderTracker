package com.codems.ordertracker.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Request body for updating an order that is still awaiting payment")
public record UpdateOrderRequest(

        @Schema(example = "Baku, Nizami street 12, apt. 5")
        @NotBlank(message = "Shipping address is required")
        @Size(max = 500, message = "Shipping address must be at most 500 characters")
        String shippingAddress,

        @Valid
        @NotEmpty(message = "An order must contain at least one item")
        @Size(max = 100, message = "An order may contain at most 100 items")
        List<OrderItemRequest> items
) {
}
