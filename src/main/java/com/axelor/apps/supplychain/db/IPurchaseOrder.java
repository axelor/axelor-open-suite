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
package com.axelor.apps.supplychain.db;

public interface IPurchaseOrder {

	
	/**
	 * Static purchase order status select
	 */

	static final int STATUS_DRAFT = 1;
	static final int STATUS_REQUESTED = 2;
	static final int STATUS_RECEIVED = 3;
	static final int STATUS_VALIDATED = 4;
	static final int STATUS_FINISHED = 5;
	static final int STATUS_CANCELED = 6;
	
	
	/**
	 * Static invoicing type select
	 */

	static final int INVOICING_FREE = 1;
	static final int INVOICING_BY_DELIVERY = 2;
	static final int INVOICING_PER_ORDER = 3;
	
	
}