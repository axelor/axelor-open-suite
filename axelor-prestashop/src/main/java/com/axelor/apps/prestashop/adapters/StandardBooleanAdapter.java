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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * And sometimes, Prestashop marshals boolean like the rest of the world…
 *
 */
public class StandardBooleanAdapter extends XmlAdapter<String, Boolean> {
	@Override
	public Boolean unmarshal(String v) throws Exception {
		return v == null ? null : "true".equals(v);
	}

	@Override
	public String marshal(Boolean v) throws Exception {
		return v == null ? null : (v ? "true" : "false");
	}
}
