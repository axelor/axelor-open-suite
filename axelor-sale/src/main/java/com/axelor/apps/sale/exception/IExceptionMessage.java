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
package com.axelor.apps.sale.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	/**
	 * Sales Order Stock Move Service
	 */
	static final String SALES_ORDER_STOCK_MOVE_1 = /*$$(*/ "Invoice by delivery impose that all sale order lines must have service or stockable product with provision from stock" /*)*/ ;
	
	/**
	 * Sales Order Service Impl
	 */
	static final String SALES_ORDER_1 = /*$$(*/ "The company %s doesn't have any configured sequence for sale orders" /*)*/ ;
	
	/**
	 * Sale Config Service
	 */
	static final String SALE_CONFIG_1 = /*$$(*/ "%s :\n You must configure Sales module for company %s" /*)*/ ;
}
