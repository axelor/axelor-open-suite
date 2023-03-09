/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import static com.axelor.apps.tool.MetaJsonFieldType.BOOLEAN;
import static com.axelor.apps.tool.MetaJsonFieldType.DATE;
import static com.axelor.apps.tool.MetaJsonFieldType.DATETIME;
import static com.axelor.apps.tool.MetaJsonFieldType.DECIMAL;
import static com.axelor.apps.tool.MetaJsonFieldType.INTEGER;
import static com.axelor.apps.tool.MetaJsonFieldType.JSON_MANY_TO_MANY;
import static com.axelor.apps.tool.MetaJsonFieldType.JSON_MANY_TO_ONE;
import static com.axelor.apps.tool.MetaJsonFieldType.JSON_ONE_TO_MANY;
import static com.axelor.apps.tool.MetaJsonFieldType.LONG;
import static com.axelor.apps.tool.MetaJsonFieldType.MANY_TO_MANY;
import static com.axelor.apps.tool.MetaJsonFieldType.MANY_TO_ONE;
import static com.axelor.apps.tool.MetaJsonFieldType.ONE_TO_MANY;
import static com.axelor.apps.tool.MetaJsonFieldType.STRING;
import static com.axelor.apps.tool.MetaJsonFieldType.TIME;

import com.axelor.apps.tool.exception.ToolExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
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
          I18n.get(ToolExceptionMessage.ERROR_CONVERT_JSON_TYPE_TO_TYPE),
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
          I18n.get(ToolExceptionMessage.ERROR_CONVERT_TYPE_TO_JSON_TYPE),
          nameType);
    }
    return jsonTypeName;
  }

  private static Map<String, String> createTypeToJsonTypeMap() {
    Map<String, String> typeToJsonTypeMap = new HashMap<>();
    typeToJsonTypeMap.put("String", STRING);
    typeToJsonTypeMap.put("Integer", INTEGER);
    typeToJsonTypeMap.put("Long", LONG);
    typeToJsonTypeMap.put("BigDecimal", DECIMAL);
    typeToJsonTypeMap.put("Boolean", BOOLEAN);
    typeToJsonTypeMap.put("LocalDateTime", DATETIME);
    typeToJsonTypeMap.put("LocalDate", DATE);
    typeToJsonTypeMap.put("LocalTime", TIME);
    typeToJsonTypeMap.put("ManyToOne", MANY_TO_ONE);
    typeToJsonTypeMap.put("ManyToMany", MANY_TO_MANY);
    typeToJsonTypeMap.put("OneToMany", ONE_TO_MANY);
    typeToJsonTypeMap.put("Custom-ManyToOne", JSON_MANY_TO_ONE);
    typeToJsonTypeMap.put("Custom-ManyToMany", JSON_MANY_TO_MANY);
    typeToJsonTypeMap.put("Custom-OneToMany", JSON_ONE_TO_MANY);
    return typeToJsonTypeMap;
  }

  /**
   * Get Model class name of wantedType in case wantedType is a ManyToOne or Custom-ManyToOne. This
   * method return wantedType if wantedType is not a ManyToOne or Custom-ManyToOne
   *
   * @param indicator
   * @param wantedType
   * @return
   */
  public static String getWantedClassName(MetaJsonField indicator, String wantedType) {
    String wantedClassName;
    if ((wantedType.equals("ManyToOne") || wantedType.equals("Custom-ManyToOne"))
        && indicator.getTargetModel() != null) {
      // it is a relational field so we get the target model class
      String targetName = indicator.getTargetModel();
      // get only the class without the package
      wantedClassName = targetName.substring(targetName.lastIndexOf('.') + 1);
    } else {
      wantedClassName = wantedType;
    }
    return wantedClassName;
  }
}
