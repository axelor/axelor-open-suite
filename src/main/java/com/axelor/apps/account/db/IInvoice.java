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
	
	// STATUS SELECT
	static final int STATUS_DRAFT = 1;
	static final int STATUS_VALIDATED = 2;
	static final int STATUS_VENTILATED = 3;
	static final int STATUS_CANCELED = 4;

}
