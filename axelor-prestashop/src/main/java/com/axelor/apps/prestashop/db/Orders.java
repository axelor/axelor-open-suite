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

public class Orders extends Base {
	
	private String id_shop_group;
	
	private String id_shop;
	
	private String reference;
	
	private String id_address_delivery;
	
	private String id_address_invoice;
	
	private String id_cart;
	
	private String id_currency;
	
	private String id_lang;
	
	private String id_customer;
	
	private String id_carrier;
	
	private String total_paid_tax_incl;
	
	private String total_wrapping_tax_incl;
	
	private String total_paid;
	
	private String total_paid_tax_excl;
	
	private String total_paid_real;
	
	private String total_products_wt;
	
	private String total_shipping;
	
	private String total_products;
	
	private String total_shipping_tax_incl;
	
	private String total_shipping_tax_excl;
	
	private String conversion_rate;
	
	private String module;
	
	private String payment;
	
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

	public String getReference() {
		return reference;
	}
	
	public void setReference(String reference) {
		this.reference = reference;
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

	public String getId_cart() {
		return id_cart;
	}

	public void setId_cart(String id_cart) {
		this.id_cart = id_cart;
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

	public String getId_customer() {
		return id_customer;
	}

	public void setId_customer(String id_customer) {
		this.id_customer = id_customer;
	}

	public String getId_carrier() {
		return id_carrier;
	}

	public void setId_carrier(String id_carrier) {
		this.id_carrier = id_carrier;
	}

	public String getTotal_paid_tax_incl() {
		return total_paid_tax_incl;
	}

	public void setTotal_paid_tax_incl(String total_paid_tax_incl) {
		this.total_paid_tax_incl = total_paid_tax_incl;
	}

	public String getTotal_wrapping_tax_incl() {
		return total_wrapping_tax_incl;
	}

	public void setTotal_wrapping_tax_incl(String total_wrapping_tax_incl) {
		this.total_wrapping_tax_incl = total_wrapping_tax_incl;
	}

	public String getTotal_paid() {
		return total_paid;
	}

	public void setTotal_paid(String total_paid) {
		this.total_paid = total_paid;
	}

	public String getTotal_paid_tax_excl() {
		return total_paid_tax_excl;
	}

	public void setTotal_paid_tax_excl(String total_paid_tax_excl) {
		this.total_paid_tax_excl = total_paid_tax_excl;
	}

	public String getTotal_paid_real() {
		return total_paid_real;
	}

	public void setTotal_paid_real(String total_paid_real) {
		this.total_paid_real = total_paid_real;
	}

	public String getTotal_products_wt() {
		return total_products_wt;
	}

	public void setTotal_products_wt(String total_products_wt) {
		this.total_products_wt = total_products_wt;
	}

	public String getTotal_shipping() {
		return total_shipping;
	}

	public void setTotal_shipping(String total_shipping) {
		this.total_shipping = total_shipping;
	}

	public String getTotal_products() {
		return total_products;
	}

	public void setTotal_products(String total_products) {
		this.total_products = total_products;
	}

	public String getTotal_shipping_tax_incl() {
		return total_shipping_tax_incl;
	}

	public void setTotal_shipping_tax_incl(String total_shipping_tax_incl) {
		this.total_shipping_tax_incl = total_shipping_tax_incl;
	}

	public String getTotal_shipping_tax_excl() {
		return total_shipping_tax_excl;
	}

	public void setTotal_shipping_tax_excl(String total_shipping_tax_excl) {
		this.total_shipping_tax_excl = total_shipping_tax_excl;
	}

	public String getConversion_rate() {
		return conversion_rate;
	}

	public void setConversion_rate(String conversion_rate) {
		this.conversion_rate = conversion_rate;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
	}

	public Associations getAssociations() {
		return associations;
	}

	public void setAssociations(Associations associations) {
		this.associations = associations;
	}

	@Override
	public String toString() {
		return "Orders [id_shop_group=" + id_shop_group + ", id_shop=" + id_shop + ", reference=" + reference
				+ ", id_address_delivery=" + id_address_delivery + ", id_address_invoice=" + id_address_invoice
				+ ", id_cart=" + id_cart + ", id_currency=" + id_currency + ", id_lang=" + id_lang + ", id_customer="
				+ id_customer + ", id_carrier=" + id_carrier + ", total_paid_tax_incl=" + total_paid_tax_incl
				+ ", total_wrapping_tax_incl=" + total_wrapping_tax_incl + ", total_paid=" + total_paid
				+ ", total_paid_tax_excl=" + total_paid_tax_excl + ", total_paid_real=" + total_paid_real
				+ ", total_products_wt=" + total_products_wt + ", total_shipping=" + total_shipping
				+ ", total_products=" + total_products + ", total_shipping_tax_incl=" + total_shipping_tax_incl
				+ ", total_shipping_tax_excl=" + total_shipping_tax_excl + ", conversion_rate=" + conversion_rate
				+ ", module=" + module + ", payment=" + payment + ", associations=" + associations + "]";
	}
}
