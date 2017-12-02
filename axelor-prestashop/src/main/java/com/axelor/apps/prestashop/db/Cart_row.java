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

public class Cart_row {

	private String id_address_delivery;

	private String id_product;

	private String id_product_attribute;

	private String quantity;

	public String getId_address_delivery() {
		return id_address_delivery;
	}

	public void setId_address_delivery(String id_address_delivery) {
		this.id_address_delivery = id_address_delivery;
	}

	public String getId_product() {
		return id_product;
	}

	public void setId_product(String id_product) {
		this.id_product = id_product;
	}

	public String getId_product_attribute() {
		return id_product_attribute;
	}

	public void setId_product_attribute(String id_product_attribute) {
		this.id_product_attribute = id_product_attribute;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "Cart_row [id_address_delivery=" + id_address_delivery + ", id_product=" + id_product
				+ ", id_product_attribute=" + id_product_attribute + ", quantity=" + quantity + "]";
	}
}
