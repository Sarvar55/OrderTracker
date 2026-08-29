package com.codems.ordertracker.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Aggregated order and webhook statistics for admins")
public record DashboardStatsResponse(
        long totalOrders,
        Map<String, Long> ordersByStatus,
        long totalWebhookEvents,
        Map<String, Long> webhookEventsByStatus,
        double webhookSuccessRate
) {
}