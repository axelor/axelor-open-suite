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
package com.axelor.apps.base.db;

/**
 * Interface of Product package. Enum all static variable of packages.
 * 
 */
public interface IProduct {

	// APPLICATION TYPE SELECT VALUE
	static final int APPLICATION_TYPE_PRODUCT = 1;
	static final int APPLICATION_TYPE_PROFILE = 2;
	static final int APPLICATION_TYPE_EXPENSE = 3;

	// PRODUCT TYPE SELECT
	static final String PRODUCT_TYPE_SERVICE = "service";
	static final String PRODUCT_TYPE_STORABLE = "storable";
	
	// SALE TRACKING ORDER SELECT
	static final int SALE_TRACKING_ORDER_FIFO = 1;
	static final int SALE_TRACKING_ORDER_LIFO = 2;
	
	// SALE SUPPLY SELECT
	static final int SALE_SUPPLY_FROM_STOCK = 1;
	static final int SALE_SUPPLY_PURCHASE = 2;
	static final int SALE_SUPPLY_PRODUCE = 3;
	
}
