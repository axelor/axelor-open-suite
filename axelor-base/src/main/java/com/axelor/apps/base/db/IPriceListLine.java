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
 * Interface of PriceListLine object. Enum all static variable of packages.
 * 
 */
public interface IPriceListLine {

	/**
	 * Static select for PriceListLine
	 */
	
	// AMOUNT TYPE SELECT
	static final int AMOUNT_TYPE_PERCENT = 1;
	static final int AMOUNT_TYPE_FIXED = 2;
	static final int AMOUNT_TYPE_NONE = 3;
	
	// AMOUNT TYPE SELECT
	static final int TYPE_DISCOUNT = 1;
	static final int TYPE_ADDITIONNAL = 2;
	static final int TYPE_REPLACE = 3;
	
}
