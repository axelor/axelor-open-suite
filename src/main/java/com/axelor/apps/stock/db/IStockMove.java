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

public interface IStockMove {

	/**
	 * Static stock move status select
	 */

	static final int STATUS_DRAFT = 1;
	static final int STATUS_PLANNED = 2;
	static final int STATUS_REALIZED = 3;
	static final int STATUS_CANCELED = 4;

	/**
	 * Static StockMove type select
	 */
	
	static final int TYPE_INTERNAL = 1;
	static final int TYPE_OUTGOING = 2;
	static final int TYPE_INCOMING = 3;
}
