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

public class Products extends Base {

	private String id_category_default;
	
	private Categories categories;
	
	private String price;
	
	private String width;
	
	private String minimal_quantity;
	
	private String on_sale;
	
	private String active;
	
	private String available_for_order;
	
	private String show_price;
	
	private String state;
	
	private Language name;
	
	private Language description;
	
	private Language link_rewrite;

	public String getId_category_default() {
		return id_category_default;
	}

	public void setId_category_default(String id_category_default) {
		this.id_category_default = id_category_default;
	}

	public Categories getCategories() {
		return categories;
	}

	public void setCategories(Categories categories) {
		this.categories = categories;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getMinimal_quantity() {
		return minimal_quantity;
	}

	public void setMinimal_quantity(String minimal_quantity) {
		this.minimal_quantity = minimal_quantity;
	}

	public String getOn_sale() {
		return on_sale;
	}

	public void setOn_sale(String on_sale) {
		this.on_sale = on_sale;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getAvailable_for_order() {
		return available_for_order;
	}

	public void setAvailable_for_order(String available_for_order) {
		this.available_for_order = available_for_order;
	}

	public String getShow_price() {
		return show_price;
	}

	public void setShow_price(String show_price) {
		this.show_price = show_price;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Language getName() {
		return name;
	}
	
	public void setName(Language name) {
		this.name = name;
	}

	public Language getDescription() {
		return description;
	}

	public void setDescription(Language description) {
		this.description = description;
	}

	public Language getLink_rewrite() {
		return link_rewrite;
	}

	public void setLink_rewrite(Language link_rewrite) {
		this.link_rewrite = link_rewrite;
	}

	@Override
	public String toString() {
		return "Products [id_category_default=" + id_category_default + ", categories=" + categories + ", price="
				+ price + ", width=" + width + ", minimal_quantity=" + minimal_quantity + ", on_sale=" + on_sale
				+ ", active=" + active + ", available_for_order=" + available_for_order + ", show_price=" + show_price
				+ ", state=" + state + ", name=" + name + ", description=" + description + ", link_rewrite="
				+ link_rewrite + "]";
	}
}
