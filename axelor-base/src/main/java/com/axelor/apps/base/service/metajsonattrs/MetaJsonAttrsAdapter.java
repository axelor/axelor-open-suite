/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.metajsonattrs;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.utils.MetaTool;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaJsonAttrsAdapter {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Map.Entry<String, Object> adaptValueForMap(
      MetaJsonField metaJsonField, Object value) throws AxelorException {

    Objects.requireNonNull(value);
    String wantedType = MetaTool.jsonTypeToType(metaJsonField.getType());
    String fieldName = metaJsonField.getName();
    logger.debug("Adapting value {} into {}", value, wantedType);
    switch (wantedType) {
      case "LocalDateTime":
      case "LocalDate":
      case "LocalTime":
        if (!(value instanceof Temporal)) {
          break;
        }
        return new AbstractMap.SimpleEntry<>(fieldName, value.toString());
      case "ManyToOne":
      case "Custom-ManyToOne":
        // Can be a instance of map
        if (value instanceof Map) {
          return new AbstractMap.SimpleEntry<>(fieldName, value);
        }
        if (value instanceof Model) {
          return new AbstractMap.SimpleEntry<>(fieldName, modelToJson((Model) value));
        }
        break;
      case "String":
        if (!(value instanceof String)) {
          break;
        }
        return new AbstractMap.SimpleEntry<>(fieldName, value);
      case "Boolean":
        if (!(value instanceof Boolean)) {
          break;
        }
        return new AbstractMap.SimpleEntry<>(fieldName, value);
      case "Integer":
        if (!(value instanceof Integer)) {
          break;
        }
        return new AbstractMap.SimpleEntry<>(fieldName, value);
      case "Long":
        if (!(value instanceof Long)) {
          break;
        }
        return new AbstractMap.SimpleEntry<>(fieldName, value);
      case "BigDecimal":
        if (!(value instanceof BigDecimal)) {
          break;
        }
        return new AbstractMap.SimpleEntry<>(fieldName, value);
      case "OneToMany":
      case "Custom-OneToMany":
      case "ManyToMany":
      case "Custom-ManyToMany":
        if (!(value instanceof List)) {
          break;
        }
        return new AbstractMap.SimpleEntry<>(fieldName, toMapList(value));
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.META_JSON_TYPE_NOT_MANAGED),
            fieldName);
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_INCONSISTENCY,
        I18n.get(BaseExceptionMessage.META_JSON_TYPE_NO_MATCH_OBJECT_VALUE),
        fieldName,
        value);
  }

  static List<Map<String, Object>> toMapList(Object value) {
    List<?> values = (List<?>) value;
    List<Map<String, Object>> result;
    if (!values.isEmpty()) {
      if (values.get(0) instanceof Map) {
        return (List<Map<String, Object>>) values;
      } else if (values.get(0) instanceof Model) {
        return values.stream()
            .map(model -> modelToJson((Model) model))
            .collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  /**
   * This method map a Model in json that need to be used in a OneToMany type. Map will be in the
   * form of <"id", model.id>
   *
   * @param model
   * @return
   */
  static Map<String, Object> modelToJson(Model model) {
    final Map<String, Object> manyToOneObject = new HashMap<>();
    manyToOneObject.put("id", model.getId());
    return manyToOneObject;
  }
}
