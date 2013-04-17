package com.axelor.exception.db;

/**
 * Interface of Exception package. Enum all static variable of packages.
 * 
 * @author belloy
 * 
 */
public interface IException {
	
	/**
	 * Type select
	 */
	public static final int TECHNICAL = 0;
	public static final int FUNCTIONNAL = 1;
	
	/**
	 * Category select
	 */
	public static final int MISSING_FIELD = 1;
	public static final int NO_UNIQUE_KEY = 2;
	public static final int NO_VALUE = 3;
	public static final int CONFIGURATION_ERROR = 4;
	public static final int INCONSISTENCY = 5;
	
	/**
	 * Origin select
	 */
	public static final String INVOICE_ORIGIN = "invoice";
	public static final String REMINDER = "reminder";
	public static final String DOUBTFUL_CUSTOMER = "doubtfulCustomer";
	public static final String REIMBURSEMENT = "reimbursement";
	public static final String DIRECT_DEBIT = "directDebit";
	public static final String INTERBANK_PAYMENT_ORDER = "interbankPaymentOrder";



}
