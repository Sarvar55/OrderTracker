package com.codems.ordertracker.domain.base;

public final class HibernateFilters {

	public static final String ACTIVE_RECORD_FILTER = "activeRecordFilter";
	public static final String ACTIVE_RECORD_CONDITION = "status <> 'DELETED'";

	private HibernateFilters() {
	}
}
