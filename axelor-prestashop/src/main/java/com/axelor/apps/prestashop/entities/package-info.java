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
@XmlJavaTypeAdapters({
	@XmlJavaTypeAdapter(type=boolean.class, value=PrestashopBooleanAdapter.class),
	@XmlJavaTypeAdapter(type=LocalDate.class, value=PrestashopLocalDateAdapter.class),
	@XmlJavaTypeAdapter(type=LocalDateTime.class, value=PrestashopLocalDateTimeAdapter.class)
})
package com.axelor.apps.prestashop.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import com.axelor.apps.prestashop.adapters.PrestashopBooleanAdapter;
import com.axelor.apps.prestashop.adapters.PrestashopLocalDateAdapter;
import com.axelor.apps.prestashop.adapters.PrestashopLocalDateTimeAdapter;