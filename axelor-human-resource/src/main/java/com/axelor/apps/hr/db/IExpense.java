package com.axelor.apps.hr.db;

public interface IExpense {
	static final int STATUS_DRAFT = 1;
	static final int STATUS_CONFIRMED = 2;
	static final int STATUS_VALIDATED = 3;
	static final int STATUS_REFUSED = 4;
	static final int STATUS_CANCELED = 5;
}
