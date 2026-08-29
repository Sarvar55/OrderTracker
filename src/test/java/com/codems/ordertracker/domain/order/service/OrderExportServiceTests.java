package com.codems.ordertracker.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codems.ordertracker.common.security.model.SecurityUser;
import com.codems.ordertracker.domain.order.entity.Order;
import com.codems.ordertracker.domain.order.entity.OrderItem;
import com.codems.ordertracker.domain.order.repository.OrderRepository;
import com.codems.ordertracker.domain.user.entity.User;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderExportServiceTests {

    private static final Long CURRENT_USER_ID = 1L;

    @Mock
    private OrderRepository orderRepository;

    private OrderExportService orderExportService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        orderExportService = new OrderExportService(orderRepository);

        currentUser = User.of("sarvar", "sarvar@example.com", "encoded");
        currentUser.setId(CURRENT_USER_ID);
        authenticateAs(currentUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("exports orders as a CSV with a header row and one row per order")
    void exportsOrdersAsCsv() {
        Order order = Order.of(currentUser, "ORD-20260827-4F2A9C31", "USD", "Baku, Nizami street 12");
        order.addItem(OrderItem.of("Keyboard", "KB-8721", 1, new BigDecimal("149.90")));

        when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(order));

        byte[] csv = orderExportService.exportMyOrdersAsCsv(null, null, null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertThat(content).startsWith("Order Number,Status,Total Amount,Currency,Items");
        assertThat(content).contains("ORD-20260827-4F2A9C31");
        assertThat(content).contains("149.90");
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(SecurityUser.from(user), null, List.of()));
    }
}