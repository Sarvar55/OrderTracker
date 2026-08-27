package com.codems.ordertracker.domain.order.controller;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import com.codems.ordertracker.domain.base.BaseResponse;
import com.codems.ordertracker.domain.base.PageResponse;
import com.codems.ordertracker.domain.order.dto.CancelOrderRequest;
import com.codems.ordertracker.domain.order.dto.CreateOrderRequest;
import com.codems.ordertracker.domain.order.dto.OrderResponse;
import com.codems.ordertracker.domain.order.dto.OrderStatusHistoryResponse;
import com.codems.ordertracker.domain.order.dto.OrderSummaryResponse;
import com.codems.ordertracker.domain.order.dto.UpdateOrderRequest;
import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Create, browse, update and cancel orders of the authenticated customer")
public class OrderController {

    private final OrderService orderService;

    @PostMapping(version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Create an order",
            description = "Creates an order for the authenticated customer. The total amount is calculated from "
                    + "the supplied items and the order starts in PENDING_PAYMENT."
    )
    public ResponseEntity<BaseResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(created, HttpStatus.CREATED, "Order created successfully"));
    }

    @GetMapping(version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "List my orders",
            description = "Returns a paginated list of the authenticated customer's orders, optionally filtered "
                    + "by status and creation date range."
    )
    public ResponseEntity<BaseResponse<PageResponse<OrderSummaryResponse>>> findMyOrders(
            @Parameter(description = "Filter by order status")
            @RequestParam(required = false) OrderStatus status,

            @Parameter(description = "Only orders created at or after this timestamp")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Only orders created at or before this timestamp")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.success(orderService.findMyOrders(status, from, to, pageable)));
    }

    @GetMapping(value = "/{id}", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(summary = "Get an order by id", description = "Returns a single order including items and status history.")
    public ResponseEntity<BaseResponse<OrderResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success(orderService.getById(id)));
    }

    @GetMapping(value = "/number/{orderNumber}", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Get an order by order number",
            description = "Looks up an order by the business identifier shown to customers, e.g. ORD-20260827-4F2A9C31."
    )
    public ResponseEntity<BaseResponse<OrderResponse>> findByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(BaseResponse.success(orderService.getByOrderNumber(orderNumber)));
    }

    @GetMapping(value = "/{id}/status-history", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Get the status history of an order",
            description = "Returns every status change of the order, including the ones triggered by payment "
                    + "and shipment webhooks."
    )
    public ResponseEntity<BaseResponse<List<OrderStatusHistoryResponse>>> statusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success(orderService.getStatusHistory(id)));
    }

    @PutMapping(value = "/{id}", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Update an order",
            description = "Replaces the shipping address and the items of an order. Only allowed while the order "
                    + "is still in PENDING_PAYMENT."
    )
    public ResponseEntity<BaseResponse<OrderResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.success(
                orderService.update(id, request), HttpStatus.OK, "Order updated successfully"));
    }

    @PatchMapping(value = "/{id}/cancel", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Cancel an order",
            description = "Cancels an order that has not been shipped yet and records the reason in the status history."
    )
    public ResponseEntity<BaseResponse<OrderResponse>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CancelOrderRequest request
    ) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(BaseResponse.success(
                orderService.cancel(id, reason), HttpStatus.OK, "Order cancelled successfully"));
    }

    @DeleteMapping(value = "/{id}", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Delete an order",
            description = "Soft deletes a cancelled or delivered order so it no longer shows up in the customer's list."
    )
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
