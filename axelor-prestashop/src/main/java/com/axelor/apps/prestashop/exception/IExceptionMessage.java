/*
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
package com.axelor.apps.prestashop.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {

	/**
	 * Batch operation
	 */
	static final String BATCH_IMPORT = /*$$(*/ "Import completed" /*)*/;
	static final String BATCH_EXPORT = /*$$(*/ "Export completed" /*)*/;

	/**
	 * Base batch service
	 */
	static final public String PRESTASHOP_BATCH_1 = /*$$(*/ "Unknown action %s for the %s prestashop" /*)*/;
	static final public String PRESTASHOP_BATCH_2 = /*$$(*/ "Batch %s unknown" /*)*/;
	
	/**
	 * Batch log
	 */
	static final String INVALID_CURRENCY = /*$$(*/ "Currency code /name is null or invalid" /*)*/;
	static final String INVALID_COUNTRY = /*$$(*/ "Country is null or invalid" /*)*/;
	static final String INVALID_CONTACT = /*$$(*/ "Contact is null or invalid" /*)*/;
	static final String INVALID_INDIVIDUAL = /*$$(*/ "Individual is null or invalid" /*)*/;
	static final String INVALID_EMAIL = /*$$(*/ "Email is null or invalid" /*)*/;
	static final String INVALID_COMPANY = /*$$(*/ "Company/Firstname is null or invalid" /*)*/;
	static final String INVALID_CITY = /*$$(*/ "City is null or invalid" /*)*/;
	static final String INVALID_PRODUCT_CATEGORY = /*$$(*/ "Product Category is null or invalid" /*)*/;
	static final String INVALID_PRODUCT = /*$$(*/ "Product Name is null or invalid" /*)*/;
	static final String INVALID_ADDRESS = /*$$(*/ "Address is null or invalid" /*)*/;
	static final String INVALID_ORDER = /*$$(*/ "Order is null or invalid" /*)*/;
	static final String INVALID_ORDER_LINE = /*$$(*/ "This order is not on prestashop or invalid product" /*)*/;
	static final String INVALID_CUSTOMER = /*$$(*/ "Customer is null or invalid" /*)*/;
}
