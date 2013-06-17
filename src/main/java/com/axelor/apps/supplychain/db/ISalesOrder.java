package com.axelor.apps.supplychain.db;

public interface ISalesOrder {

	
	/**
	 * Static salesOrder status select
	 */

	static final int DRAFT = 1;
	static final int CONFIRMED = 2;
	static final int VALIDATED = 3;
	static final int CANCELED = 4;

}
