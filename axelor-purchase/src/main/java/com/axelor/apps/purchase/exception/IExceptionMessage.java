/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
/**
 *
 */
package com.axelor.apps.purchase.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {
	
	static final String PURCHASE_ORDER_LINE_TAX_LINE = /*$$(*/ "A tax line is missing"/*)*/ ;
	/**
	 * Purchase order service impl
	 */
	static final public String PURCHASE_ORDER_1 = /*$$(*/ "The company %s doesn't have any configured sequence for the purchase orders" /*)*/;

	/**
	 * Purchase config service
	 */
	static final public String PURCHASE_CONFIG_1 = /*$$(*/ "%s : You must configure Purchase module for the company %s" /*)*/;

	/**
	 * Merge purchase order
	 */

	static final public String PURCHASE_ORDER_MERGE_ERROR_CURRENCY = /*$$(*/ "The currency is required and must be the same for all purchase orders" /*)*/;
	static final public String PURCHASE_ORDER_MERGE_ERROR_SUPPLIER_PARTNER = /*$$(*/ "The supplier Partner is required and must be the same for all purchase orders" /*)*/;
	static final public String PURCHASE_ORDER_MERGE_ERROR_COMPANY = /*$$(*/ "The company is required and must be the same for all purchase orders" /*)*/;
}
