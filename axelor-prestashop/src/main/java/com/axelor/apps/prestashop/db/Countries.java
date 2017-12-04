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

public class Countries extends Base {
	
	private Language name;
	
	private String iso_code;
	
	private String id_zone;
	
	private String contains_states;
	
	private String need_identification_number;
	
	private String display_tax_label;
	
	private String active;
	
	public Countries() {}

	public Language getName() {
		return name;
	}

	public void setName(Language name) {
		this.name = name;
	}

	public String getIso_code() {
		return iso_code;
	}

	public void setIso_code(String iso_code) {
		this.iso_code = iso_code;
	}

	public String getId_zone() {
		return id_zone;
	}

	public void setId_zone(String id_zone) {
		this.id_zone = id_zone;
	}

	public String getContains_states() {
		return contains_states;
	}

	public void setContains_states(String contains_states) {
		this.contains_states = contains_states;
	}

	public String getNeed_identification_number() {
		return need_identification_number;
	}

	public void setNeed_identification_number(String need_identification_number) {
		this.need_identification_number = need_identification_number;
	}

	public String getDisplay_tax_label() {
		return display_tax_label;
	}

	public void setDisplay_tax_label(String display_tax_label) {
		this.display_tax_label = display_tax_label;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "Countries [name=" + name + ", iso_code=" + iso_code + ", id_zone=" + id_zone + ", contains_states="
				+ contains_states + ", need_identification_number=" + need_identification_number
				+ ", display_tax_label=" + display_tax_label + ", active=" + active + "]";
	}
}
