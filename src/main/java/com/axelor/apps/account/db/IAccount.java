/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.db;

/**
 * Interface of Contract package. Enum all static variable of packages.
 */
public interface IAccount {

	/**
	 * Static select in PaymentScheduleExport
	 */

	// EXPORT TYPE
	static final int MONTHLY_EXPORT = 0;
	static final int INVOICE_EXPORT = 1;
	static final int MEMORY_EXPORT = 2;

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
	// NATURE
	static final int PAYMENT_SCHEDULE = 0;
	static final int MONTHLY_PAYMENT_SCHEDULE = 1;
	static final int MAJOR_ACCOUNT_SCHEDULE = 2;

	// IRRECOVERABLE STATE SELECT
	static final int NOT_IRRECOUVRABLE = 0;
	static final int TO_PASS_IN_IRRECOUVRABLE = 1;
	static final int PASSED_IN_IRRECOUVRABLE = 2;

	// TYPE
	static final String MONTHLY_TYPE = "EMS";
	static final String INDEBTEDNESS_TYPE = "ESU";
	static final String RJ_TYPE = "ERJ";
	static final String LJ_TYPE = "ELJ";
	static final String DELAY_PAYMENT_TYPE = "EDL";
	static final String USHER_TYPE = "EHU";

	// SUB TYPE
	static final String MONTHLY_SUB_TYPE = "MSU";
	static final String INDEBTEDNESS_SUB_TYPE = "PSS";
	static final String RJ_SUB_TYPE = "RJU";
	static final String LJ_SUB_TYPE = "LJU";
	static final String DELAY_PAYMENT_SUB_TYPE = "DPM";
	static final String USHER_SUB_TYPE = "HUI";
	static final String SOCIAL_SUB_TYPE = "AID";
	static final String FR_BANK_SUB_TYPE = "BDF";

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
	// MAIL
	static final int NO_SENDED = 0;
	static final int TO_SEND = 1;
	static final int SENDED = 2;

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
	 * Static select in AccountClearance
	 */
	// CONTRACTLINE TYPE SELECT
	static final int CANCELED_CONTRACTLINE = 0;
	static final int NOT_CANCELED_CONTRACTLINE = 1;
	static final int ALL_CONTRACTLINE = 2;

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

}
