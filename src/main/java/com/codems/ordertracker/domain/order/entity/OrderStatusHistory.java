package com.codems.ordertracker.domain.order.entity;

import com.codems.ordertracker.domain.base.BaseEntity;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_status_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class OrderStatusHistory extends BaseEntity {

	public static final String SOURCE_CUSTOMER = "CUSTOMER";
	public static final String SOURCE_SYSTEM = "SYSTEM";

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(name = "previous_status", length = 30)
	private OrderStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 30)
	private OrderStatus newStatus;

	@Column(length = 500)
	private String reason;

	@Column(nullable = false, length = 50)
	private String source;

	public static OrderStatusHistory of(
			Order order,
			OrderStatus previousStatus,
			OrderStatus newStatus,
			String reason,
			String source
	) {
		OrderStatusHistory history = new OrderStatusHistory();
		history.setOrder(order);
		history.setPreviousStatus(previousStatus);
		history.setNewStatus(newStatus);
		history.setReason(reason);
		history.setSource(source == null ? SOURCE_SYSTEM : source);
		return history;
	}
}
