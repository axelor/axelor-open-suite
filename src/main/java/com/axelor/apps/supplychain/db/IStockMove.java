package com.axelor.apps.supplychain.db;

public interface IStockMove {

	/**
	 * Static StockMove status select
	 */

	static final int DRAFT = 1;
	static final int PLANNED = 2;
	static final int REALIZED = 3;
	static final int CANCELED = 4;

	/**
	 * Static StockMove type select
	 */
	
	static final int INTERNAL = 1;
	static final int OUTGOING = 2;
	static final int INCOMING = 3;
}
