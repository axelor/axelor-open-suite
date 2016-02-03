/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.db;

/**
 * Interface of Administration package. Enum all static variable of packages.
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
	static final String TERM_INVOICE = "termInvoice";
	static final String INVOICE = "invoice";
	static final String PAYMENT_SCHEDULE = "paymentSchedule";
	static final String PAYMENT_VOUCHER = "paymentVoucher";
	static final String PAYMENT_VOUCHER_RECEIPT_NUMBER = "paymentVoucherReceiptNo";
	static final String DEBIT = "debit";
	static final String MOVE_LINE_REPORT = "moveLineReport";
	static final String REIMBOURSEMENT = "reimbursement";
	static final String ACCOUNT_CLEARANCE = "accountClearance";
	static final String IRRECOVERABLE = "irrecoverable";
	static final String CHEQUE_REJECT = "chequeReject";
	static final String SALES_INTERFACE = "saleInterface";
	static final String REFUND_INTERFACE = "refundInterface";
	static final String TREASURY_INTERFACE = "treasuryInterface";
	static final String PURCHASE_INTERFACE = "purchaseInterface";
	static final String MOVE_LINE_EXPORT = "moveLineExport";
	static final String DOUBTFUL_CUSTOMER = "doubtfulCustomer";
	static final String SALES_ORDER = "saleOrder";
	static final String PURCHASE_ORDER = "purchaseOrder";
	static final String EVENT_TICKET = "eventTicket";
	static final String INTERNAL = "intStockMove";
	static final String OUTGOING = "outStockMove";
	static final String INCOMING = "inStockMove";
	static final String PRODUCT_TRACKING_NUMBER = "productTrackingNumber";
	static final String PRO_TRAINING = "proTraining";
	static final String PRODUCTION_ORDER = "productionOrder";
	static final String MANUF_ORDER = "manufOrder";
	
	
	/**
	 * Static select in General
	 */

	// TYPE
	static final int MAP_API_GOOGLE = 1;
	static final int MAP_API_OSM = 2;
	
	// NB DECIMALS
	static final int DEFAULT_NB_DECIMAL_DIGITS = 2;
}
