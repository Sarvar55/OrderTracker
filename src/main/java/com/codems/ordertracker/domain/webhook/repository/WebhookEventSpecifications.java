package com.codems.ordertracker.domain.webhook.repository;

import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEvent;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class WebhookEventSpecifications {

    private WebhookEventSpecifications() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Specification<WebhookEvent> hasChannel(WebhookChannel channel) {
        if (channel == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("channel"), channel);
    }

    public static Specification<WebhookEvent> hasStatus(WebhookEventStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<WebhookEvent> createdAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<WebhookEvent> createdBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}