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
package com.axelor.apps.prestashop.adapters;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PrestashopLocalDateAdapter extends XmlAdapter<String, LocalDate> {
	private static final String NULL_DATE = "0000-00-00";

	@Override
	public LocalDate unmarshal(String v) throws Exception {
		return v == null || NULL_DATE.equals(v) ? null : LocalDate.parse(v);
	}

	@Override
	public String marshal(LocalDate v) throws Exception {
		return v == null ? NULL_DATE : v.toString();
	}

}
