/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db;

/**
 * Interface of Account package. Enum all static variable of packages.
 */
public interface IAccount {

	/**
	 * Static select in PaymentScheduleExport
	 */

	// EXPORT TYPE
	static final int MONTHLY_EXPORT = 0;
	static final int INVOICE_EXPORT = 1;

	// STATE
	static final int DRAFT_EXPORT = 0;
	static final int VALIDATED_EXPORT = 1;

	/**
	 * Static select in PaymentScheduleImport
	 */

	// STATE
	static final int DRAFT_IMPORT = 0;
	static final int VALIDATED_IMPORT = 1;

	/**
	 * Static select in PaymentSchedule
	 */

	// IRRECOVERABLE STATE SELECT
	static final int NOT_IRRECOUVRABLE = 0;
	static final int TO_PASS_IN_IRRECOUVRABLE = 1;
	static final int PASSED_IN_IRRECOUVRABLE = 2;

	/**
	 * Static select in Move
	 */
	// STATE
	static final String DRAFT_MOVE = "draft";
	static final String SIMULATED_MOVE = "simulated";
	static final String VALIDATED_MOVE = "validated";
	static final String CANCELED_MOVE = "canceled";

	/**
	 * Static select in MoveLine
	 */

	// REIMBURSEMENT STATE
	static final int NULL = 0;
	static final int REIMBURSING = 1;
	static final int REIMBURSED = 2;

	// IRRECOVERABLE STATE SELECT
	// see PaymentSchedule

	/**
	 * Static select in CashRegisterLine
	 */
	// STATE
	static final int DRAFT_CASHREGISTERLINE = 0;
	static final int CLOSED_CASHREGISTERLINE = 1;

	/**
	 * Static select in Reimbursement
	 */

	/**
	 * Static select in ReimbursementExport
	 */

	/**
	 * Static select in InterbankPaymentOrderImport
	 */

	// STATE
	static final int DRAFT_IPO_IMPORT = 0;
	static final int VALIDATED_IPO_IMPORT = 1;

	/**
	 * Static select in InterbankPaymentOrderImport
	 */

	// TYPE SELECT
	static final int DEBIT_IMPORT = 0;
	static final int REIMBURSEMENT_IMPORT = 1;
//	static final int INTERBANK_PAYMENT_ORDER_REJECT_IMPORT = 2;
	
	
	/**
	 * Static select in AccountingBatch
	 */

	// ACTION TYPE
	static final int BATCH_REIMBURSEMENT = 11;
	static final int BATCH_DIRECT_DEBIT = 12;
	static final int BATCH_REMINDER = 14;
	static final int BATCH_INTERBANK_PAYMENT_ORDER = 15;
	static final int BATCH_DOUBTFUL_CUSTOMER = 16;
	static final int BATCH_ACCOUNT_CUSTOMER = 17;
	static final int BATCH_MOVE_LINE_EXPORT = 18;
	
	// REIMBURSEMENT TYPE
	static final int BATCH_REIMBURSEMENT_EXPORT = 1;
	static final int BATCH_REIMBURSEMENT_IMPORT = 2;
	
	// REIMBURSEMENT EXPORT TYPE
	static final int REIMBURSEMENT_EXPORT_GENERATE = 1;
	static final int REIMBURSEMNT_EXPORT_EXPORT = 2;

	// REIMBURSEMENT TYPE
	static final int BATCH_DIRECT_DEBIT_EXPORT = 1;
	static final int BATCH_DIRECT_DEBIT_IMPORT = 2;
	
	// REIMBURSEMENT TYPE
	static final int INTERBANK_PAYMENT_ORDER_IMPORT = 1;
	static final int INTERBANK_PAYMENT_ORDER_REJECT_IMPORT = 2;

	/**
	 * Static select in AccountingBatch
	 */

	// REMINDER TYPE
	static final int REMINDER = 1;

	
	
	/**
	 * Static select in MoveLineReport
	 */

	// REPORT TYPE SELECT
	static final int REPORT_PAYMENT_COLLECTION_JOURNAL = 1;
	static final int REPORT_GENERAL_LEDGER = 2;
	static final int REPORT_BALANCE = 3;
	static final int REPORT_AGED_BALANCE = 4;
	static final int REPORT_CHEQUE_DEPOSIT = 5;
	static final int REPORT_CASH_PAYMENTS = 10;
	static final int REPORT_JOURNAL = 11;
	static final int REPORT_VAT_STATEMENT = 12;
	static final int REPORT_PAYMENT_DIFFERENCES = 13;
	
	// REPORT TYPE SELECT EXPORT
	static final int EXPORT_SALES = 6;
	static final int EXPORT_REFUNDS = 7;
	static final int EXPORT_TREASURY = 8;
	static final int EXPORT_PURCHASES = 9;
	
	
	/**
	 * Static select in Reconcile
	 */
	// STATUS SELECT
	static final int RECONCILE_STATUS_DRAFT = 1;
	static final int RECONCILE_STATUS_CONFIRMED = 2;
	static final int RECONCILE_STATUS_CANCELED = 3;
	
}
