package com.codems.ordertracker.domain.order.mapper;

import com.codems.ordertracker.domain.order.dto.OrderItemResponse;
import com.codems.ordertracker.domain.order.dto.OrderResponse;
import com.codems.ordertracker.domain.order.dto.OrderStatusHistoryResponse;
import com.codems.ordertracker.domain.order.dto.OrderSummaryResponse;
import com.codems.ordertracker.domain.order.entity.Order;
import com.codems.ordertracker.domain.order.entity.OrderItem;
import com.codems.ordertracker.domain.order.entity.OrderStatusHistory;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer() == null ? null : order.getCustomer().getId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getShippingAddress(),
                order.getPaymentReference(),
                order.getTrackingNumber(),
                toItemResponses(order.getItems()),
                toHistoryResponses(order.getStatusHistory()),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }

    public List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream().map(this::toItemResponse).toList();
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductName(),
                item.getProductSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    public List<OrderStatusHistoryResponse> toHistoryResponses(List<OrderStatusHistory> history) {
        return history.stream().map(this::toHistoryResponse).toList();
    }

    public OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getReason(),
                history.getSource(),
                history.getCreatedAt()
        );
    }
}
