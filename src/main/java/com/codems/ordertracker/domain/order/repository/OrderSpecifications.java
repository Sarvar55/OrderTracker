package com.codems.ordertracker.domain.order.repository;

import com.codems.ordertracker.domain.order.entity.Order;
import com.codems.ordertracker.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Specification<Order> ownedBy(Long customerId) {
        return (root, query, builder) -> builder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("orderStatus"), status);
    }

    public static Specification<Order> createdAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Order> createdBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
