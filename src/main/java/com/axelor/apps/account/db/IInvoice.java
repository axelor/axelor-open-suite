package com.axelor.apps.account.db;

/**
 * Interface of Invoice package. Enum all static variable of packages.
 * 
 * @author guerrier
 * 
 */
public interface IInvoice {

	static final int NONE = 0;

	/**
	 * Static select in Invoice
	 */

	// OPERATION TYPE SELECT
	static final int SUPPLIER_PURCHASE = 1;
	static final int SUPPLIER_REFUND = 2;
	static final int CLIENT_SALE = 3;
	static final int CLIENT_REFUND = 4;

	// IRRECOVERABLE STATE SELECT
	static final int NOT_IRRECOUVRABLE = 0;
	static final int TO_PASS_IN_IRRECOUVRABLE = 1;
	static final int PASSED_IN_IRRECOUVRABLE = 2;
	

	/**
	 * Static select in InvoiceLine
	 */


	/**
	 * Static select in InvoiceBatch
	 */

	// ACTION TYPE
	static final int BATCH_INVOICE = 0;
	static final int BATCH_STATUS = 1;
	static final int BATCH_ALARM = 2;

	// TOSTATUSSELECT
	static final String TO_VAL = "tov";
	static final String TO_DIS = "val";

}
