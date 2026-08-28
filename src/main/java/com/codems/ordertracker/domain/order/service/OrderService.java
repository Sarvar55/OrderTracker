package com.codems.ordertracker.domain.order.service;

import com.codems.ordertracker.common.exceptions.types.CommonErrorType;
import com.codems.ordertracker.common.exceptions.types.BaseException;
import com.codems.ordertracker.common.util.ApplicationUtility;
import com.codems.ordertracker.domain.base.PageResponse;
import com.codems.ordertracker.domain.order.dto.CreateOrderRequest;
import com.codems.ordertracker.domain.order.dto.OrderItemRequest;
import com.codems.ordertracker.domain.order.dto.OrderResponse;
import com.codems.ordertracker.domain.order.dto.OrderStatusHistoryResponse;
import com.codems.ordertracker.domain.order.dto.OrderSummaryResponse;
import com.codems.ordertracker.domain.order.dto.UpdateOrderRequest;
import com.codems.ordertracker.domain.order.entity.Order;
import com.codems.ordertracker.domain.order.entity.OrderItem;
import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.order.entity.OrderStatusHistory;
import com.codems.ordertracker.domain.order.exception.InvalidOrderStatusTransitionException;
import com.codems.ordertracker.domain.order.exception.OrderAccessDeniedException;
import com.codems.ordertracker.domain.order.exception.OrderNotFoundException;
import com.codems.ordertracker.domain.order.exception.OrderNotModifiableException;
import com.codems.ordertracker.domain.order.mapper.OrderMapper;
import com.codems.ordertracker.domain.order.repository.OrderRepository;
import com.codems.ordertracker.domain.order.repository.OrderSpecifications;
import com.codems.ordertracker.domain.user.entity.User;
import com.codems.ordertracker.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final Set<OrderStatus> DELETABLE_STATUSES =
            EnumSet.of(OrderStatus.CANCELLED, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        User customer = currentUser();
        Order order = Order.of(
                customer,
                orderNumberGenerator.generate(),
                request.currency(),
                request.shippingAddress()
        );
        request.items().forEach(item -> order.addItem(toItem(item)));
        order.changeStatus(OrderStatus.PENDING_PAYMENT, "Order created", OrderStatusHistory.SOURCE_CUSTOMER);

        Order saved = orderRepository.save(order);
        log.info("Order {} created by user {} with {} item(s), total {} {}",
                saved.getOrderNumber(), customer.getId(), saved.getItems().size(),
                saved.getTotalAmount(), saved.getCurrency());

        return orderMapper.toResponse(saved);
    }

    public OrderResponse getById(Long id) {
        return orderMapper.toResponse(requireOwnedOrder(id));
    }

    public OrderResponse getByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> OrderNotFoundException.byOrderNumber(orderNumber));
        assertOwnership(order);
        return orderMapper.toResponse(order);
    }

    public PageResponse<OrderSummaryResponse> findMyOrders(
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        Specification<Order> specification = OrderSpecifications.ownedBy(currentUserId());
        specification = and(specification, OrderSpecifications.hasStatus(status));
        specification = and(specification, OrderSpecifications.createdAfter(from));
        specification = and(specification, OrderSpecifications.createdBefore(to));

        Page<OrderSummaryResponse> page = orderRepository.findAll(specification, pageable)
                .map(orderMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    public List<OrderStatusHistoryResponse> getStatusHistory(Long id) {
        return orderMapper.toHistoryResponses(requireOwnedOrder(id).getStatusHistory());
    }

    @Transactional
    public OrderResponse update(Long id, UpdateOrderRequest request) {
        Order order = requireOwnedOrder(id);
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            throw OrderNotModifiableException.of(order.getOrderStatus());
        }

        order.setShippingAddress(request.shippingAddress());
        order.clearItems();
        request.items().forEach(item -> order.addItem(toItem(item)));

        log.info("Order {} updated, new total {} {}",
                order.getOrderNumber(), order.getTotalAmount(), order.getCurrency());

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long id, String reason) {
        Order order = requireOwnedOrder(id);
        if (!order.getOrderStatus().isCancellable()) {
            throw InvalidOrderStatusTransitionException.of(order.getOrderStatus(), OrderStatus.CANCELLED);
        }

        order.changeStatus(
                OrderStatus.CANCELLED,
                reason == null || reason.isBlank() ? "Cancelled by customer" : reason,
                OrderStatusHistory.SOURCE_CUSTOMER
        );
        log.info("Order {} cancelled by user {}", order.getOrderNumber(), currentUserId());

        return orderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        Order order = requireOwnedOrder(id);
        if (!DELETABLE_STATUSES.contains(order.getOrderStatus())) {
            throw OrderNotModifiableException.of(order.getOrderStatus());
        }

        orderRepository.delete(order);
        log.info("Order {} soft deleted by user {}", order.getOrderNumber(), currentUserId());
    }

    /**
     * Applies a status change coming from outside the customer flow, e.g. from
     * the payment or shipment webhook handlers. Validates the transition, keeps
     * the audit trail up to date and returns the updated order so the caller can
     * trigger notifications.
     *
     * @param orderNumber business identifier carried by the external event
     * @param target      status the external system reported
     * @param reason      human readable explanation stored in the audit trail
     * @param source      who triggered the change, e.g. {@code PAYMENT_WEBHOOK}
     */
    @Transactional
    public Order applyExternalStatusChange(
            String orderNumber,
            OrderStatus target,
            String reason,
            String source
    ) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> OrderNotFoundException.byOrderNumber(orderNumber));
        return applyStatusChange(order, target, reason, source);
    }

    /** Same as {@link #applyExternalStatusChange}, for callers that already loaded the order. */
    @Transactional
    public Order applyStatusChange(Order order, OrderStatus target, String reason, String source) {
        OrderStatus current = order.getOrderStatus();
        if (current == target) {
            log.debug("Order {} already in status {}, ignoring duplicate event", order.getOrderNumber(), target);
            return order;
        }
        if (!current.canTransitionTo(target)) {
            throw InvalidOrderStatusTransitionException.of(current, target);
        }

        order.changeStatus(target, reason, source);
        log.info("Order {} moved from {} to {} by {}", order.getOrderNumber(), current, target, source);

        return order;
    }

    /** Stores the gateway reference so later payment events can resolve the order. */
    @Transactional
    public Order attachPaymentReference(String orderNumber, String paymentReference) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> OrderNotFoundException.byOrderNumber(orderNumber));
        order.setPaymentReference(paymentReference);
        return order;
    }

    /** Stores the carrier tracking number reported by the shipment webhook. */
    @Transactional
    public Order attachTrackingNumber(String orderNumber, String trackingNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> OrderNotFoundException.byOrderNumber(orderNumber));
        order.setTrackingNumber(trackingNumber);
        return order;
    }

    private Order requireOwnedOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> OrderNotFoundException.byId(id));
        assertOwnership(order);
        return order;
    }

    private void assertOwnership(Order order) {
        if (!order.isOwnedBy(currentUserId())) {
            log.warn("User {} tried to access order {}", currentUserId(), order.getId());
            throw OrderAccessDeniedException.of();
        }
    }

    private OrderItem toItem(OrderItemRequest request) {
        return OrderItem.of(
                request.productName(),
                request.productSku(),
                request.quantity(),
                request.unitPrice()
        );
    }

    private User currentUser() {
        Long userId = currentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(CommonErrorType.UNAUTHORIZED));
    }

    private Long currentUserId() {
        return ApplicationUtility.getCurrentUserId()
                .orElseThrow(() -> BaseException.of(CommonErrorType.UNAUTHORIZED));
    }

    private Specification<Order> and(Specification<Order> base, Specification<Order> extra) {
        return extra == null ? base : base.and(extra);
    }
}
