/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
 * Interface of PriceList object. Enum all static variable of packages.
 *
 */
public interface IPriceList {

	/**
	 * Static select for PriceList
	 */

	// TYPE SELECT
	public static final int TYPE_SALE = 1;
	public static final int TYPE_PURCHASE = 2;

	// BASED ON SELECT
	public static final int BASED_ON_COST_PRICE = 1;
	public static final int BASED_ON_SALE_PRICE = 2;

}
