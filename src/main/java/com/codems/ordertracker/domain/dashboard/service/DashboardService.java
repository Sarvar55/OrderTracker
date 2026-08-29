package com.codems.ordertracker.domain.dashboard.service;

import com.codems.ordertracker.domain.dashboard.dto.DashboardStatsResponse;
import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.order.repository.OrderRepository;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import com.codems.ordertracker.domain.webhook.repository.WebhookEventRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final WebhookEventRepository webhookEventRepository;

    public DashboardStatsResponse getStats() {
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.count(
                    (root, query, builder) -> builder.equal(root.get("orderStatus"), status)));
        }

        Map<String, Long> webhooksByStatus = new LinkedHashMap<>();
        for (WebhookEventStatus status : WebhookEventStatus.values()) {
            webhooksByStatus.put(status.name(), webhookEventRepository.countByStatus(status));
        }

        long totalOrders = orderRepository.count();
        long totalWebhookEvents = webhookEventRepository.count();
        long processed = webhooksByStatus.getOrDefault(WebhookEventStatus.PROCESSED.name(), 0L);
        double successRate = totalWebhookEvents == 0 ? 0.0 : (processed * 100.0) / totalWebhookEvents;

        return new DashboardStatsResponse(totalOrders, ordersByStatus, totalWebhookEvents, webhooksByStatus, successRate);
    }
}