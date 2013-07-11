package com.axelor.apps.base.db;

/**
 * Interface of Administration package. Enum all static variable of packages.
 * 
 * @author guerrier
 * 
 */
public interface IAdministration {

	/**
	 * Static select export type
	 */

	static final String PDF = "pdf";
	static final String XLS = "xls";

	/**
	 * Static select month
	 */

	static final int JAN = 1;
	static final int FEB = 2;
	static final int MAR = 3;
	static final int APR = 4;
	static final int MAY = 5;
	static final int JUN = 6;
	static final int JUL = 7;
	static final int AUG = 8;
	static final int SEP = 9;
	static final int OCT = 10;
	static final int NOV = 11;
	static final int DEC = 12;

	/**
	 * Static select yes/no
	 */

	static final int YES = 1;
	static final int NO = 0;

	/**
	 * Static select in Sequence
	 */

	// TYPE
	static final String INVENTORY = "inventory";
	static final String PARTNER = "partner";
	static final String MOVE = "move";
	static final String CUSTOMER_INVOICE = "customerInvoice";
	static final String INVOICE_DRAFT = "invoiceDraft";
	static final String TERM_INVOICE = "termInvoice";
	static final String VIRTUAL_INVOICE = "virtualInvoice";
	static final String STANDARD_INVOICE = "standardInvoice";
	static final String CUSTOMER_REFUND = "customerRefund";
	static final String CUSTOMER_REFUND_DRAFT = "customerRefundDraft";
	static final String PAYMENT_SCHEDULE = "paymentSchedule";
	static final String PAYMENT_VOUCHER = "paymentVoucher";
	static final String PAYMENT_VOUCHER_RECEIPT_NUMBER = "paymentVoucherReceiptNo";
	static final String DEBIT = "debit";
	static final String DEBIT_REJECT = "debitReject";
	static final String MOVE_LINE_REPORT = "moveLineReport";
	static final String REIMBOURSEMENT = "reimbursement";
	static final String ACCOUNT_CLEARANCE = "accountClearance";
	static final String IRRECOVERABLE = "irrecoverable";
	static final String CHEQUE_REJECT = "chequeReject";
	static final String SALES_INTERFACE = "salesInterface";
	static final String REFUND_INTERFACE = "refundInterface";
	static final String TREASURY_INTERFACE = "treasuryInterface";
	static final String PURCHASE_INTERFACE = "purchaseInterface";
	static final String MOVE_LINE_EXPORT = "moveLineExport";
	static final String DOUBTFUL_CUSTOMER = "doubtfulCustomer";
	static final String SALES_ORDER = "salesOrder";
	static final String PURCHASE_ORDER = "purchaseOrder";
	static final String EVENT_TICKET = "eventTicket";
	static final String INTERNAL = "intStockMove";
	static final String OUTGOING = "outStockMove";
	static final String INCOMING = "inStockMove";
	
}
