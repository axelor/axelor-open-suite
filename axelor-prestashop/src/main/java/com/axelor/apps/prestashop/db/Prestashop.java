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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name = "prestashop")
@XmlSeeAlso({Currencies.class, Countries.class, Customers.class, Addresses.class,
			Categories.class, Products.class, Carts.class, Orders.class, Order_histories.class, Order_details.class})
public class Prestashop {
	
	private Object prestashop;

	public Prestashop() {}
	
	@XmlElement
	public Object getPrestashop() {
		return prestashop;
	}

	public void setPrestashop(Object prestashop) {
		this.prestashop = prestashop;
	}

	@Override
	public String toString() {
		return "PrestaShop [prestashop=" + prestashop + "]";
	}
}
