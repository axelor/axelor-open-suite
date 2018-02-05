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

public class Cart_rows {

	private List<Cart_row> cart_row;

	public Cart_rows() {}

	public List<Cart_row> getCart_row() {
		return cart_row;
	}

	public void setCart_row(List<Cart_row> cart_row) {
		this.cart_row = cart_row;
	}

	@Override
	public String toString() {
		return "Cart_rows [cart_row=" + cart_row + "]";
	}
}
