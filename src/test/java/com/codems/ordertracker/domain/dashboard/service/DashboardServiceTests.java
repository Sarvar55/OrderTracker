package com.codems.ordertracker.domain.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codems.ordertracker.domain.dashboard.dto.DashboardStatsResponse;
import com.codems.ordertracker.domain.order.repository.OrderRepository;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import com.codems.ordertracker.domain.webhook.repository.WebhookEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WebhookEventRepository webhookEventRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(orderRepository, webhookEventRepository);

        when(orderRepository.count(any(Specification.class))).thenReturn(0L);
        when(orderRepository.count()).thenReturn(10L);
        when(webhookEventRepository.countByStatus(any())).thenReturn(0L);
        when(webhookEventRepository.countByStatus(WebhookEventStatus.PROCESSED)).thenReturn(8L);
        when(webhookEventRepository.count()).thenReturn(10L);
    }

    @Test
    @DisplayName("computes the webhook success rate from processed and total events")
    void computesWebhookSuccessRate() {
        DashboardStatsResponse stats = dashboardService.getStats();

        assertThat(stats.totalOrders()).isEqualTo(10L);
        assertThat(stats.totalWebhookEvents()).isEqualTo(10L);
        assertThat(stats.webhookSuccessRate()).isEqualTo(80.0);
    }
}