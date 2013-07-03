package com.axelor.apps.supplychain.db;

public interface IStockMove {

	/**
	 * Static StockMove status select
	 */

	static final int DRAFT = 1;
	static final int CONFIRMED = 2;
	static final int REALIZED = 3;
	static final int CANCELED = 4;
	
	// TYPE SELECT
	static final String INTERNAL = "intStockMove";
	static final String OUTGOING = "outStockMove";
	static final String INCOMING = "inStockMove";
}
