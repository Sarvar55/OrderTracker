package com.codems.ordertracker.domain.order.repository;

import com.codems.ordertracker.domain.order.entity.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByPaymentReference(String paymentReference);

    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    boolean existsByOrderNumber(String orderNumber);
}
