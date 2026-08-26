package com.codems.ordertracker.domain.user.entity;

import com.codems.ordertracker.domain.base.BaseEntity;
import com.codems.ordertracker.domain.base.HibernateFilters;
import com.codems.ordertracker.domain.base.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@Entity
@Table(name = "users")
@SQLDelete(sql = "update users set status = 'DELETED', deleted_at = current_timestamp, updated_at = current_timestamp where id = ?")
@FilterDef(
		name = HibernateFilters.ACTIVE_RECORD_FILTER,
		defaultCondition = HibernateFilters.ACTIVE_RECORD_CONDITION,
		autoEnabled = true,
		applyToLoadByKey = true
)
@Filter(name = HibernateFilters.ACTIVE_RECORD_FILTER)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends BaseEntity {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String username;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private RecordStatus status = RecordStatus.ACTIVE;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static User of(String username, String email, String password) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(password);
		return user;
	}

}
