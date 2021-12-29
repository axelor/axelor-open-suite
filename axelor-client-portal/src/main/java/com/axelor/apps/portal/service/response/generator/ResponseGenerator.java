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
package com.axelor.apps.portal.service.response.generator;

import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ResponseGenerator {

  protected final List<String> modelFields = new ArrayList<>();
  protected final Map<String, Function<Object, Object>> extraFieldMap = new HashMap<>();
  protected final Map<String, Object> parent = new HashMap<>();
  protected Class<?> classType = null;

  public abstract void init();

  public Map<String, Object> generate(Object object) {
    return generate(object, new HashMap<>());
  }

  public Map<String, Object> generate(Object object, Map<String, Object> parent) {
    init();
    this.parent.putAll(parent);
    return pepareResponse(object);
  }

  private Map<String, Object> pepareResponse(Object object) {
    Map<String, Object> response = new HashMap<>();
    setModelFieldValues(object, response);
    setExtraFields(object, response);
    return response;
  }

  private void setExtraFields(Object object, Map<String, Object> response) {
    for (Entry<String, Function<Object, Object>> entry : extraFieldMap.entrySet()) {
      response.put(entry.getKey(), entry.getValue().apply(object));
    }
  }

  private void setModelFieldValues(Object object, Map<String, Object> response) {
    Mapper mapper = Mapper.of(classType);
    for (String field : modelFields) {

      Property property = mapper.getProperty(field);
      if (property == null) {
        continue;
      }
      Object value = mapper.get(object, field);
      if (value == null) {
        response.put(field, value);
        continue;
      }

      if (property.isReference()
          && ResponseGeneratorFactory.isValid(property.getTarget().getName())) {
        Map<String, Object> parent = Mapper.toMap(object);
        parent.put("_model", classType.getName());
        Map<String, Object> values =
            ResponseGeneratorFactory.of(property.getTarget().getName()).generate(value, parent);
        value = values;
      }

      if (property.isCollection()
          && ResponseGeneratorFactory.isValid(property.getTarget().getName())
          && value instanceof Collection<?>
          && !((Collection<?>) value).isEmpty()) {
        ResponseGenerator generator = ResponseGeneratorFactory.of(property.getTarget().getName());
        Map<String, Object> parent = Mapper.toMap(object);
        parent.put("_model", classType.getName());
        value =
            ((Collection<?>) value)
                .stream()
                    .map((item) -> generator.generate(item, parent))
                    .collect(Collectors.toList());
      }

      response.put(field, value);
    }
  }
}
