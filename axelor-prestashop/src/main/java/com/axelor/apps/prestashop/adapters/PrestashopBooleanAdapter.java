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
 * Adapter for brainfucked Prestashop boolean rendering
 *
 */
public class PrestashopBooleanAdapter extends XmlAdapter<Integer, Boolean> {

	@Override
	public Boolean unmarshal(Integer v) throws Exception {
		return v == null ? null : v != 0;
	}

	@Override
	public Integer marshal(Boolean v) throws Exception {
		return v == null ? null : (v ? 1 : 0);
	}

}
