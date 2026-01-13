/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.indicator;

import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.google.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class IndicatorExportServiceImpl implements IndicatorExportService {

  @Override
  public <T> String toCsv(List<T> beans) {
    if (ObjectUtils.isEmpty(beans)) {
      return "";
    }

    List<Map<String, Object>> rows = beans.stream().map(this::map).collect(Collectors.toList());
    Set<String> headers = rows.get(0).keySet();

    StringBuilder sb = new StringBuilder();
    sb.append(String.join(",", headers)).append("\n");

    for (Map<String, Object> row : rows) {
      String line =
          headers.stream()
              .map(h -> row.get(h) != null ? row.get(h).toString().replace(",", " ") : "")
              .collect(Collectors.joining(","));
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

  private Map<String, Object> map(Object bean) {
    Mapper mapper = Mapper.of(bean.getClass());
    Map<String, Object> data = new LinkedHashMap<>();
    Property[] properties = mapper.getProperties();

    Stream.of(properties)
        .filter(p -> "id".equals(p.getName()))
        .forEach(p -> put(data, mapper, bean, p));
    Stream.of(properties).filter(Property::isNameColumn).forEach(p -> put(data, mapper, bean, p));
    Stream.of(properties)
        .filter(p -> !("id".equals(p.getName()) || p.isNameColumn()))
        .forEach(p -> put(data, mapper, bean, p));

    return data;
  }

  private void put(Map<String, Object> data, Mapper mapper, Object bean, Property property) {
    if (property.isCollection()
        || property.isTransient()
        || property.isImage()
        || property.isEncrypted()
        || property.isPassword()) {
      return;
    }
    Object value = mapper.get(bean, property.getName());
    if (property.isReference() && value != null) {
      Mapper target = Mapper.of(property.getTarget());
      value = target.get(value, property.getTargetName());
    }
    data.put(property.getName(), value);
  }
}
