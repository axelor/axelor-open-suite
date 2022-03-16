/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.wordreport.config;

import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public interface WordReportHelperService {

  public List<Object> getAllElementFromObject(Object obj, Class<?> toSearch);

  public Property getProperty(Mapper mapper, String propertyName);

  public Mapper getMapper(String modelFullName) throws ClassNotFoundException;

  public ImmutablePair<Property, Object> findField(final Mapper mapper, Object value, String name);

  public Object findNameColumn(Property targetField, Object value);

  public String getDateTimeFormat(Object value);

  public Pair<String, Boolean> checkHideWrapper(String value, Object bean);

  public Pair<String, String> checkIfElseWrapper(String value, Object bean)
      throws IOException, AxelorException;

  public int getBigDecimalScale();
}
