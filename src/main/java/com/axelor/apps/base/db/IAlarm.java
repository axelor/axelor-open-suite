package com.axelor.apps.base.db;

/**
 * Interface of Alarm package. Enum all static variable of packages.
 * 
 * @author guerrier
 * 
 */
public interface IAlarm {

	/**
	 * Static select for Alarm & Message
	 */

	// TYPE
	static final String INVOICE = "invoice";
	static final String PAYMENT = "payment";
	static final String REJECT = "reject";
	
	// TYPE
	static final int INVOICING_MANAGER = 1;
	static final int CONTRACT_MANAGER = 2;
	static final int COMMERCIAL_MANAGER = 3;
	static final int TECHNICAL_MANAGER = 4;
}
