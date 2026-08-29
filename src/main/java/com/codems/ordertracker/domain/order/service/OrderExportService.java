package com.codems.ordertracker.domain.order.service;

import com.codems.ordertracker.common.exceptions.types.BaseException;
import com.codems.ordertracker.common.exceptions.types.CommonErrorType;
import com.codems.ordertracker.common.util.ApplicationUtility;
import com.codems.ordertracker.domain.order.entity.Order;
import com.codems.ordertracker.domain.order.entity.OrderStatus;
import com.codems.ordertracker.domain.order.repository.OrderRepository;
import com.codems.ordertracker.domain.order.repository.OrderSpecifications;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderExportService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] HEADERS = {
            "Order Number", "Status", "Total Amount", "Currency", "Items", "Payment Reference",
            "Tracking Number", "Created At", "Updated At"
    };

    private final OrderRepository orderRepository;

    public byte[] exportMyOrdersAsCsv(OrderStatus status, LocalDateTime from, LocalDateTime to) {
        Long customerId = ApplicationUtility.getCurrentUserId()
                .orElseThrow(() -> BaseException.of(CommonErrorType.UNAUTHORIZED));

        Specification<Order> specification = OrderSpecifications.ownedBy(customerId);
        specification = and(specification, OrderSpecifications.hasStatus(status));
        specification = and(specification, OrderSpecifications.createdAfter(from));
        specification = and(specification, OrderSpecifications.createdBefore(to));

        List<Order> orders = orderRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toCsv(orders);
    }

    private byte[] toCsv(List<Order> orders) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(buffer, true, StandardCharsets.UTF_8)) {
            writer.println(String.join(",", HEADERS));
            orders.forEach(order -> writer.println(toCsvRow(order)));
        }
        return buffer.toByteArray();
    }

    private String toCsvRow(Order order) {
        return String.join(",",
                escape(order.getOrderNumber()),
                escape(order.getOrderStatus().name()),
                escape(order.getTotalAmount().toPlainString()),
                escape(order.getCurrency()),
                String.valueOf(order.getItems().size()),
                escape(order.getPaymentReference()),
                escape(order.getTrackingNumber()),
                escape(order.getCreatedAt() == null ? "" : order.getCreatedAt().format(TIMESTAMP_FORMAT)),
                escape(order.getUpdatedAt() == null ? "" : order.getUpdatedAt().format(TIMESTAMP_FORMAT))
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\"") ? "\"" + escaped + "\"" : escaped;
    }

    private Specification<Order> and(Specification<Order> base, Specification<Order> extra) {
        return extra == null ? base : base.and(extra);
    }
}