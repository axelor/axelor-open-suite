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
package com.axelor.apps.prestashop.entities;

/**
 * Type of resources handled by Prestashop's API.
 * Only those actually used by Axelor sync are declared,
 * feel free to enhance.
 */
public enum PrestashopResourceType {
	ADDRESSES("addresses"),
	CARTS("carts"),
	COUNTRIES("countries"),
	CURRENCIES("currencies"),
	CUSTOMERS("customers"),
	DELIVERIES("deliveries"),
	IMAGES("images"),
	LANGUAGES("languages"),
	ORDER_DETAILS("order_details"),
	ORDER_HISTORIES("order_histories"),
	ORDER_INVOICES("order_invoices"),
	ORDER_PAYMENTS("order_payments"),
	ORDER_STATUSES("order_states"),
	ORDERS("orders"),
	PRODUCT_CATEGORIES("categories"),
	PRODUCTS("products"),
	// No typo… really
	STOCK_AVAILABLES("stock_availables");

	final String label;

	private PrestashopResourceType(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}