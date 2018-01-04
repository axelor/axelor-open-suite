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

public class Categories extends Base {

	private String active;
	
	private String id_parent;
	
	private Language name;
	
	private Language link_rewrite;
	
	public Categories() {}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getId_parent() {
		return id_parent;
	}

	public void setId_parent(String id_parent) {
		this.id_parent = id_parent;
	}

	public Language getName() {
		return name;
	}

	public void setName(Language name) {
		this.name = name;
	}

	public Language getLink_rewrite() {
		return link_rewrite;
	}

	public void setLink_rewrite(Language link_rewrite) {
		this.link_rewrite = link_rewrite;
	}

	@Override
	public String toString() {
		return "Categories [active=" + active + ", id_parent=" + id_parent + ", name=" + name + ", link_rewrite="
				+ link_rewrite + "]";
	}
}
