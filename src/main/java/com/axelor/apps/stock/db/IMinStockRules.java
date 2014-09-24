/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.db;

public interface IMinStockRules {

	/**
	 * Static type select
	 */

	static final int TYPE_CURRENT = 1;
	static final int TYPE_FUTURE = 2;
	
	/**
	 * Static order alert select
	 */
	
	static final int ORDER_ALERT_ALERT = 1;
	static final int ORDER_ALERT_PURCHASE_ORDER = 2;
	static final int ORDER_ALERT_PRODUCTION_ORDER = 3;

}
