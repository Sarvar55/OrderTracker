package com.codems.ordertracker.domain.order.entity;

import com.codems.ordertracker.domain.base.BaseEntity;
import com.codems.ordertracker.domain.base.HibernateFilters;
import com.codems.ordertracker.domain.base.RecordStatus;
import com.codems.ordertracker.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@Entity
@Table(name = "orders")
@SQLDelete(sql = "update orders set status = 'DELETED', deleted_at = current_timestamp, updated_at = current_timestamp where id = ?")
@Filter(name = HibernateFilters.ACTIVE_RECORD_FILTER)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Order extends BaseEntity {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_number", nullable = false, unique = true, length = 40)
	private String orderNumber;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status", nullable = false, length = 30)
	private OrderStatus orderStatus = OrderStatus.PENDING_PAYMENT;

	@Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "shipping_address", nullable = false, length = 500)
	private String shippingAddress;

	/** Reference issued by the payment gateway, filled in by the payment webhook. */
	@Column(name = "payment_reference", length = 100)
	private String paymentReference;

	/** Carrier tracking number, filled in by the shipment webhook. */
	@Column(name = "tracking_number", length = 100)
	private String trackingNumber;

	@BatchSize(size = 50)
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<OrderItem> items = new ArrayList<>();

	@BatchSize(size = 50)
	@OrderBy("createdAt asc")
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<OrderStatusHistory> statusHistory = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private RecordStatus status = RecordStatus.ACTIVE;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static Order of(User customer, String orderNumber, String currency, String shippingAddress) {
		Order order = new Order();
		order.setCustomer(customer);
		order.setOrderNumber(orderNumber);
		order.setCurrency(currency);
		order.setShippingAddress(shippingAddress);
		return order;
	}

	public void addItem(OrderItem item) {
		item.setOrder(this);
		items.add(item);
		recalculateTotal();
	}

	public void clearItems() {
		items.clear();
		recalculateTotal();
	}

	public void recalculateTotal() {
		totalAmount = items.stream()
				.map(OrderItem::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Applies a status change and appends it to the audit trail.
	 * Callers are responsible for validating the transition first.
	 */
	public void changeStatus(OrderStatus target, String reason, String source) {
		OrderStatus previous = orderStatus;
		orderStatus = target;
		statusHistory.add(OrderStatusHistory.of(this, previous, target, reason, source));
	}

	public boolean isOwnedBy(Long userId) {
		return customer != null && customer.getId() != null && customer.getId().equals(userId);
	}
}
