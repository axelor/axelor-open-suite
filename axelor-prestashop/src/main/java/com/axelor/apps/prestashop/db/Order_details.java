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

public class Order_details extends Base {

	
	private String id_order;
	
	private String product_id;
	
	private String product_name;
	
	private String product_quantity;
	
	private String unit_price_tax_incl;
	
	private String unit_price_tax_excl;
	
	private String product_price;
	
	private String id_warehouse;
	
	private String id_shop;

	public String getId_order() {
		return id_order;
	}

	public void setId_order(String id_order) {
		this.id_order = id_order;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}

	public String getProduct_quantity() {
		return product_quantity;
	}

	public void setProduct_quantity(String product_quantity) {
		this.product_quantity = product_quantity;
	}

	public String getUnit_price_tax_incl() {
		return unit_price_tax_incl;
	}

	public void setUnit_price_tax_incl(String unit_price_tax_incl) {
		this.unit_price_tax_incl = unit_price_tax_incl;
	}

	public String getUnit_price_tax_excl() {
		return unit_price_tax_excl;
	}

	public void setUnit_price_tax_excl(String unit_price_tax_excl) {
		this.unit_price_tax_excl = unit_price_tax_excl;
	}

	public String getProduct_price() {
		return product_price;
	}

	public void setProduct_price(String product_price) {
		this.product_price = product_price;
	}

	public String getId_warehouse() {
		return id_warehouse;
	}

	public void setId_warehouse(String id_warehouse) {
		this.id_warehouse = id_warehouse;
	}

	public String getId_shop() {
		return id_shop;
	}

	public void setId_shop(String id_shop) {
		this.id_shop = id_shop;
	}

	@Override
	public String toString() {
		return "Order_details [id_order=" + id_order + ", product_id=" + product_id + ", product_name=" + product_name
				+ ", product_quantity=" + product_quantity + ", unit_price_tax_incl=" + unit_price_tax_incl
				+ ", unit_price_tax_excl=" + unit_price_tax_excl + ", product_price=" + product_price
				+ ", id_warehouse=" + id_warehouse + ", id_shop=" + id_shop + "]";
	}
}
