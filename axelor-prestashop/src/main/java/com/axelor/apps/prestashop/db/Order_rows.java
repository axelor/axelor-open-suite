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
package com.axelor.apps.prestashop.db;

import java.util.List;

public class Order_rows {
	
	private List<Order_row> order_row;

	public List<Order_row> getOrder_row() {
		return order_row;
	}

	public void setOrder_row(List<Order_row> order_row) {
		this.order_row = order_row;
	}

	@Override
	public String toString() {
		return "Order_rows [order_row=" + order_row + "]";
	}
}
