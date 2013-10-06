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
 * Interface of Invoice package. Enum all static variable of packages.
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
