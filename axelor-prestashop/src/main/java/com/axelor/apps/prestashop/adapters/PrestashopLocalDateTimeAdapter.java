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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.axelor.common.StringUtils;

public class PrestashopLocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
	private final static String NULL_DATETIME = "0000-00-00 00:00:00";
	private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public LocalDateTime unmarshal(String v) throws Exception {
		return StringUtils.isEmpty(v) || NULL_DATETIME.equals(v) ? null : LocalDateTime.parse(v, formatter);
	}

	@Override
	public String marshal(LocalDateTime v) throws Exception {
		return v == null ? NULL_DATETIME : formatter.format(v);
	}

}
