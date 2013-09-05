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
	public static final String ACCOUNT_CUSTOMER = "accountCustomer";
	public static final String MOVE_LINE_EXPORT_ORIGIN = "moveLineExport";
	public static final String IRRECOVERABLE = "irrecoverable";
	public static final String CRM = "crm";
	
}
