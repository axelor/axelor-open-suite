/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

public class Associations {

	private Cart_rows cart_rows;
	
	private Order_rows order_rows;

	public Cart_rows getCart_rows() {
		return cart_rows;
	}

	public void setCart_rows(Cart_rows cart_rows) {
		this.cart_rows = cart_rows;
	}

	public Order_rows getOrder_rows ()
    {
        return order_rows;
    }

    public void setOrder_rows (Order_rows order_rows)
    {
        this.order_rows = order_rows;
    }

    @Override
	public String toString() {
		return "Associations [cart_rows=" + cart_rows + ", order_rows=" + order_rows + "]";
	}
}
