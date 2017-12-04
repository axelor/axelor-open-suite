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

public class Carts extends Base {
	
	private String id_shop_group;
	
	private String id_shop;
	
	private String id_carrier;
	
	private String id_currency;
	
	private String id_lang;
	
	private String id_address_delivery;
	
	private String id_address_invoice;
	
	private String id_customer;
	
	private String secure_key;
	
	private Associations associations;

	public String getId_shop_group() {
		return id_shop_group;
	}

	public void setId_shop_group(String id_shop_group) {
		this.id_shop_group = id_shop_group;
	}

	public String getId_shop() {
		return id_shop;
	}

	public void setId_shop(String id_shop) {
		this.id_shop = id_shop;
	}

	public String getId_carrier() {
		return id_carrier;
	}

	public void setId_carrier(String id_carrier) {
		this.id_carrier = id_carrier;
	}

	public String getId_currency() {
		return id_currency;
	}

	public void setId_currency(String id_currency) {
		this.id_currency = id_currency;
	}

	public String getId_lang() {
		return id_lang;
	}

	public void setId_lang(String id_lang) {
		this.id_lang = id_lang;
	}

	public String getId_address_delivery() {
		return id_address_delivery;
	}

	public void setId_address_delivery(String id_address_delivery) {
		this.id_address_delivery = id_address_delivery;
	}

	public String getId_address_invoice() {
		return id_address_invoice;
	}

	public void setId_address_invoice(String id_address_invoice) {
		this.id_address_invoice = id_address_invoice;
	}

	public String getId_customer() {
		return id_customer;
	}

	public void setId_customer(String id_customer) {
		this.id_customer = id_customer;
	}
	
	public String getSecure_key() {
		return secure_key;
	}
	
	public void setSecure_key(String secure_key) {
		this.secure_key = secure_key;
	}

	public Associations getAssociations() {
		return associations;
	}

	public void setAssociations(Associations associations) {
		this.associations = associations;
	}

	@Override
	public String toString() {
		return "Carts [id_shop_group=" + id_shop_group + ", id_shop=" + id_shop + ", id_carrier=" + id_carrier
				+ ", id_currency=" + id_currency + ", id_lang=" + id_lang + ", id_address_delivery="
				+ id_address_delivery + ", id_address_invoice=" + id_address_invoice + ", id_customer=" + id_customer
				+ ", secure_key=" + secure_key + ", associations=" + associations + "]";
	}
}
