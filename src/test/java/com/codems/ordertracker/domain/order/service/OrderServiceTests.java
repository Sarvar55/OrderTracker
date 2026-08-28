package com.codems.ordertracker.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.codems.ordertracker.common.security.model.SecurityUser;
import com.codems.ordertracker.domain.order.dto.CreateOrderRequest;
import com.codems.ordertracker.domain.order.dto.OrderItemRequest;
import com.codems.ordertracker.domain.order.dto.OrderResponse;
import com.codems.ordertracker.domain.order.dto.UpdateOrderRequest;
import com.codems.ordertracker.domain.order.entity.Order;
import com.codems.ordertracker.domain.order.entity.OrderItem;
import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.order.exception.InvalidOrderStatusTransitionException;
import com.codems.ordertracker.domain.order.exception.OrderAccessDeniedException;
import com.codems.ordertracker.domain.order.exception.OrderNotFoundException;
import com.codems.ordertracker.domain.order.exception.OrderNotModifiableException;
import com.codems.ordertracker.domain.order.mapper.OrderMapper;
import com.codems.ordertracker.domain.order.repository.OrderRepository;
import com.codems.ordertracker.domain.user.entity.User;
import com.codems.ordertracker.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTests {

    private static final Long CURRENT_USER_ID = 1L;
    private static final String ORDER_NUMBER = "ORD-20260827-4F2A9C31";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    private OrderService orderService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.of("sarvar", "sarvar@example.com", "encoded");
        currentUser.setId(CURRENT_USER_ID);

        orderService = new OrderService(orderRepository, userRepository, orderNumberGenerator, new OrderMapper());

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(currentUser));
        when(orderNumberGenerator.generate()).thenReturn(ORDER_NUMBER);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authenticateAs(currentUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("creating an order sums up the line totals and starts in PENDING_PAYMENT")
    void createsOrder() {
        CreateOrderRequest request = new CreateOrderRequest(
                "USD",
                "Baku, Nizami street 12",
                List.of(
                        new OrderItemRequest("Keyboard", "KB-8721", 2, new BigDecimal("149.90")),
                        new OrderItemRequest("Mouse", "MS-1102", 1, new BigDecimal("59.00"))
                )
        );

        OrderResponse response = orderService.create(request);

        assertThat(response.orderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(response.totalAmount()).isEqualByComparingTo("358.80");
        assertThat(response.items()).hasSize(2);
        assertThat(response.statusHistory()).hasSize(1);
        assertThat(response.statusHistory().get(0).newStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("reading an order of another customer is rejected")
    void rejectsForeignOrder() {
        User other = User.of("emil", "emil@example.com", "encoded");
        other.setId(99L);
        Order order = orderOf(other, OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getById(5L))
                .isInstanceOf(OrderAccessDeniedException.class);
    }

    @Test
    @DisplayName("an unknown order id results in a not found error")
    void unknownOrder() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(404L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("an order that is already paid cannot be edited")
    void rejectsUpdateOfPaidOrder() {
        Order order = orderOf(currentUser, OrderStatus.PAID);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        UpdateOrderRequest request = new UpdateOrderRequest(
                "Baku, new address",
                List.of(new OrderItemRequest("Keyboard", "KB-8721", 1, new BigDecimal("149.90")))
        );

        assertThatThrownBy(() -> orderService.update(5L, request))
                .isInstanceOf(OrderNotModifiableException.class);
    }

    @Test
    @DisplayName("cancelling records the reason in the status history")
    void cancelsOrder() {
        Order order = orderOf(currentUser, OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancel(5L, "Ordered the wrong size");

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.statusHistory()).hasSize(1);
        assertThat(response.statusHistory().get(0).reason()).isEqualTo("Ordered the wrong size");
    }

    @Test
    @DisplayName("a shipped order can no longer be cancelled")
    void rejectsCancelOfShippedOrder() {
        Order order = orderOf(currentUser, OrderStatus.SHIPPED);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(5L, null))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    @DisplayName("a webhook driven status change is applied and audited")
    void appliesExternalStatusChange() {
        Order order = orderOf(currentUser, OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));

        Order updated = orderService.applyExternalStatusChange(
                ORDER_NUMBER, OrderStatus.PAID, "Payment captured", "PAYMENT_WEBHOOK");

        assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updated.getStatusHistory()).hasSize(1);
        assertThat(updated.getStatusHistory().get(0).getSource()).isEqualTo("PAYMENT_WEBHOOK");
    }

    @Test
    @DisplayName("a webhook event repeating the current status is ignored")
    void ignoresDuplicateWebhookEvent() {
        Order order = orderOf(currentUser, OrderStatus.PAID);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));

        Order updated = orderService.applyExternalStatusChange(
                ORDER_NUMBER, OrderStatus.PAID, "Payment captured", "PAYMENT_WEBHOOK");

        assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updated.getStatusHistory()).isEmpty();
    }

    @Test
    @DisplayName("a webhook cannot move an order backwards through the lifecycle")
    void rejectsInvalidWebhookTransition() {
        Order order = orderOf(currentUser, OrderStatus.DELIVERED);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.applyExternalStatusChange(
                ORDER_NUMBER, OrderStatus.SHIPPED, "Late event", "SHIPMENT_WEBHOOK"))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    private Order orderOf(User customer, OrderStatus status) {
        Order order = Order.of(customer, ORDER_NUMBER, "USD", "Baku, Nizami street 12");
        order.setId(5L);
        order.setOrderStatus(status);
        order.addItem(OrderItem.of("Keyboard", "KB-8721", 1, new BigDecimal("149.90")));
        return order;
    }

    private void authenticateAs(User user) {
        SecurityUser principal = SecurityUser.from(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
