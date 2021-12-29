/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.tools;

import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.rpc.ContextHandlerFactory;
import com.axelor.rpc.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ObjectTool {

  public static Map<String, Object> toMap(Object bean) {
    if (bean == null) {
      return null;
    }
    final Map<String, Object> map = new HashMap<>();
    final Mapper mapper = Mapper.of(bean.getClass());
    for (Property property : mapper.getProperties()) {
      Object value = validate(property, property.get(bean));
      map.put(property.getName(), value);
    }
    return map;
  }

  public static <T> T toBean(Class<T> klass, Map<String, Object> values) {
    final T bean;
    try {
      bean = klass.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
    if (values == null || values.isEmpty()) {
      return bean;
    }
    final Mapper mapper = Mapper.of(klass);
    for (Property property : mapper.getProperties()) {
      Object value = values.get(property.getName());
      property.set(bean, validate(property, value));
    }
    return bean;
  }

  private static Object validate(Property property, Object value) {
    if (property.isCollection() && value instanceof Collection) {
      value =
          ((Collection<?>) value)
              .stream().map(item -> createOrFind(property, item)).collect(Collectors.toList());
    } else if (property.isReference()) {
      value = createOrFind(property, value);
    }
    return value;
  }

  private static Object createOrFind(Property property, Object value) {
    if (value == null) {
      return value;
    }

    final Long id = ((Model) value).getId();
    if (id == null || id <= 0) {
      return EntityHelper.getEntity(
          ContextHandlerFactory.newHandler(property.getTarget(), Resource.toMapCompact(value))
              .getProxy());
    }
    final Object bean = JPA.em().find(property.getTarget(), id);
    return bean;
  }
}
