package com.codems.ordertracker.domain.order.service;

import com.codems.ordertracker.domain.order.repository.OrderRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PREFIX = "ORD-";
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();
    private final OrderRepository orderRepository;

    public String generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = build();
            if (!orderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique order number after " + MAX_ATTEMPTS + " attempts");
    }

    private String build() {
        String suffix = String.format("%08X", random.nextInt());
        return PREFIX + LocalDate.now().format(DATE_PART) + "-" + suffix;
    }
}
