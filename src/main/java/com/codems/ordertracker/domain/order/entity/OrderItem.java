package com.codems.ordertracker.domain.order.entity;

import com.codems.ordertracker.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class OrderItem extends BaseEntity {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "product_name", nullable = false, length = 255)
	private String productName;

	@Column(name = "product_sku", nullable = false, length = 100)
	private String productSku;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
	private BigDecimal unitPrice;

	public static OrderItem of(String productName, String productSku, Integer quantity, BigDecimal unitPrice) {
		OrderItem item = new OrderItem();
		item.setProductName(productName);
		item.setProductSku(productSku);
		item.setQuantity(quantity);
		item.setUnitPrice(unitPrice);
		return item;
	}

	public BigDecimal getLineTotal() {
		if (unitPrice == null || quantity == null) {
			return BigDecimal.ZERO;
		}
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}
}
