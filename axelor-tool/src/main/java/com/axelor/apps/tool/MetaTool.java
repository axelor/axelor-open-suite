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
package com.axelor.apps.tool;

import com.axelor.apps.tool.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Utility class containing static helper methods for meta fields and meta json fields */
public class MetaTool {

  // this class should not be instantiated
  private MetaTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Convert the type of a json field to a type of a field.
   *
   * @param nameType type of a json field
   * @return corresponding type of field
   */
  public static String jsonTypeToType(String nameType) throws AxelorException {
    Map<String, String> typeToJsonTypeMap = createTypeToJsonTypeMap();
    // reverse the map
    Map<String, String> jsonTypeToTypeMap =
        typeToJsonTypeMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    String typeName = jsonTypeToTypeMap.get(nameType);
    if (typeName == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.ERROR_CONVERT_JSON_TYPE_TO_TYPE),
          nameType);
    }
    return typeName;
  }

  public static String computeFullClassName(MetaField metaField) {
    return String.format("%s.%s", metaField.getPackageName(), metaField.getTypeName());
  }

  /**
   * Convert the type of a field to a type of a json field.
   *
   * @param nameType type of a field
   * @return corresponding type of json field
   */
  public static String typeToJsonType(String nameType) throws AxelorException {
    String jsonTypeName = createTypeToJsonTypeMap().get(nameType);
    if (jsonTypeName == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.ERROR_CONVERT_TYPE_TO_JSON_TYPE),
          nameType);
    }
    return jsonTypeName;
  }

  private static Map<String, String> createTypeToJsonTypeMap() {
    Map<String, String> typeToJsonTypeMap = new HashMap<>();
    typeToJsonTypeMap.put("String", "string");
    typeToJsonTypeMap.put("Integer", "integer");
    typeToJsonTypeMap.put("Long", "long");
    typeToJsonTypeMap.put("BigDecimal", "decimal");
    typeToJsonTypeMap.put("Boolean", "boolean");
    typeToJsonTypeMap.put("LocalDateTime", "datetime");
    typeToJsonTypeMap.put("LocalDate", "date");
    typeToJsonTypeMap.put("LocalTime", "time");
    typeToJsonTypeMap.put("ManyToOne", "many-to-one");
    typeToJsonTypeMap.put("ManyToMany", "many-to-many");
    typeToJsonTypeMap.put("OneToMany", "one-to-many");
    typeToJsonTypeMap.put("Custom-ManyToOne", "json-many-to-one");
    typeToJsonTypeMap.put("Custom-ManyToMany", "json-many-to-many");
    typeToJsonTypeMap.put("Custom-OneToMany", "json-one-to-many");
    return typeToJsonTypeMap;
  }
}
