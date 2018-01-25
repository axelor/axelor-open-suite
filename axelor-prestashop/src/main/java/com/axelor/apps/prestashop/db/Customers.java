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

public class Customers extends Base {
	
	private String company;
	
	private String firstname;
	
	private String lastname;
	
	private String max_payment_days;
	
	private String email;
	
	private String id_default_group;
	
	private String website;
	
	private String active;
	
	private String id_shop;
	
	private String id_shop_group;
	
	private String passwd;
	
	private String secure_key;
	
	public Customers() {}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getMax_payment_days() {
		return max_payment_days;
	}

	public void setMax_payment_days(String max_payment_days) {
		this.max_payment_days = max_payment_days;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getId_default_group() {
		return id_default_group;
	}

	public void setId_default_group(String id_default_group) {
		this.id_default_group = id_default_group;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}
	
	public String getId_shop() {
		return id_shop;
	}
	
	public void setId_shop(String id_shop) {
		this.id_shop = id_shop;
	}
	
	public String getId_shop_group() {
		return id_shop_group;
	}
	
	public void setId_shop_group(String id_shop_group) {
		this.id_shop_group = id_shop_group;
	}
	
	public String getPasswd() {
		return passwd;
	}
	
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getSecure_key() {
		return secure_key;
	}
	
	public void setSecure_key(String secure_key) {
		this.secure_key = secure_key;
	}
	
	@Override
	public String toString() {
		return "Customers [company=" + company + ", firstname=" + firstname + ", lastname=" + lastname
				+ ", max_payment_days=" + max_payment_days + ", email=" + email + ", id_default_group="
				+ id_default_group + ", website=" + website + ", active=" + active + ", id_shop=" + id_shop
				+ ", id_shop_group=" + id_shop_group + ", passwd=" + passwd + ", secure_key=" + secure_key + "]";
	}
}
