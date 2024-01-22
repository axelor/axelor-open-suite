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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.JsonContext;
import com.axelor.utils.MetaTool;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.temporal.Temporal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ConfiguratorMetaJsonFieldServiceImpl implements ConfiguratorMetaJsonFieldService {

  /**
   * Generate a Map for custom fields. The map entry pattern is {@code <attrNameField,
   * mapOfCustomsFields>}, with {@code attrNameField} the name of the custom field (for example
   * 'attrs') and {@code mapOfCustomsFields} the entries with the form of {@code<customFieldName,
   * value>}. Only indicators having a name with the pattern "attrFieldName$fieldName_*" ("_*" being
   * optional) will be treated.
   *
   * <p>Note: This method consumes indicators which are custom fields (i.e.: will be removed from
   * jsonIndicators).
   *
   * @param formulas
   * @param jsonIndicators
   * @return
   */
  protected Map<String, Map<String, Object>> generateAttrMap(
      List<? extends ConfiguratorFormula> formulas, JsonContext jsonIndicators) {
    // This map keys are attrs fields
    // This map values are a map<namefield, object> associated to the attr field
    // The purpose of this map is to compute it, in order to fill attr fields of Object Product
    HashMap<String, Map<String, Object>> attrValueMap = new HashMap<>();
    // Keys to remove from map, because we don't need them afterward
    List<String> keysToRemove = new ArrayList<>();

    for (Entry<String, Object> entry : jsonIndicators.entrySet()) {
      String fullName = entry.getKey();
      if (!fullName.contains("$")) {
        continue;
      }

      Object value = entry.getValue();
      String[] nameFieldInfo = fullName.split("[\\$_]");
      String attrName = nameFieldInfo[0];
      String fieldName = nameFieldInfo[1];

      formulas.forEach(
          formula -> {
            MetaJsonField metaJsonField = formula.getMetaJsonField();
            if (metaJsonField != null
                && attrName.equals(formula.getMetaField().getName())
                && fieldName.equals(metaJsonField.getName())) {
              putFieldValueInMap(fieldName, value, attrName, metaJsonField, attrValueMap);
              keysToRemove.add(fullName);
            }
          });
    }

    jsonIndicators.entrySet().removeIf(entry -> keysToRemove.contains(entry.getKey()));
    return attrValueMap;
  }

  /**
   * This method map a Model in json that need to be used in a OneToMany type. Map will be in the
   * form of <"id", model.id>
   *
   * @param model
   * @return
   */
  protected Map<String, Object> modelToJson(Model model) {
    final Map<String, Object> manyToOneObject = new HashMap<>();
    manyToOneObject.put("id", model.getId());
    return manyToOneObject;
  }

  /**
   * Method that re-map a Map that is representating a Model object. Map will be in the form of
   * <"id", model.id>
   *
   * @param map
   * @return
   */
  protected Map<String, Object> objectMapToJson(Map<String, Object> map) {

    final Map<String, Object> manyToOneObject = new HashMap<>();
    manyToOneObject.put("id", map.getOrDefault("id", null));

    return manyToOneObject;
  }

  protected void putFieldValueInMap(
      String nameField,
      Object object,
      String attrName,
      MetaJsonField metaJsonField,
      Map<String, Map<String, Object>> attrValueMap) {

    attrValueMap.computeIfAbsent(attrName, i -> new HashMap<>());

    Entry<String, Object> entry = adaptType(nameField, object, metaJsonField);
    attrValueMap.get(attrName).put(entry.getKey(), entry.getValue());
  }

  /**
   * Method that adapt type of object depending on his type.
   *
   * @param nameField
   * @param object
   * @param metaJsonField
   * @return
   */
  @SuppressWarnings("unchecked")
  protected Map.Entry<String, Object> adaptType(
      String nameField, Object object, MetaJsonField metaJsonField) {
    try {

      if (object instanceof Temporal) {
        return new AbstractMap.SimpleEntry<>(nameField, object.toString());
      }

      String wantedType = MetaTool.jsonTypeToType(metaJsonField.getType());
      // Case of many to one object
      if ("ManyToOne".equals(wantedType) || "Custom-ManyToOne".equals(wantedType)) {
        if (object instanceof Map) {
          return new AbstractMap.SimpleEntry<>(
              nameField, objectMapToJson((Map<String, Object>) object));
        }
        // The cast should not be a problem, since at this point it must be a Model
        final Map<String, Object> manyToOneObject = modelToJson((Model) object);

        return new AbstractMap.SimpleEntry<>(nameField, manyToOneObject);
      } else if ("OneToMany".equals(wantedType)
          || "Custom-OneToMany".equals(wantedType)
          || "ManyToMany".equals(wantedType)
          || "Custom-ManyToMany".equals(wantedType)) {

        List<?> listModels = (List<?>) object;
        List<Map<String, Object>> mappedList;

        if (!listModels.isEmpty() && listModels.get(0) instanceof Map) {
          mappedList =
              listModels.stream()
                  .map(model -> objectMapToJson((Map<String, Object>) model))
                  .collect(Collectors.toList());
        } else {
          mappedList =
              listModels.stream()
                  .map(model -> modelToJson((Model) model))
                  .collect(Collectors.toList());
        }

        return new AbstractMap.SimpleEntry<>(nameField, mappedList);
      }
    } catch (IllegalArgumentException | SecurityException e) {

      TraceBackService.trace(e);
    }
    return new AbstractMap.SimpleEntry<>(nameField, object);
  }

  protected <T extends Model> void fillAttrs(
      Map<String, Map<String, Object>> attrValueMap, Class<T> type, T targetObject) {

    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    attrValueMap.entrySet().stream()
        .forEach(
            attr -> {
              try {
                Map<String, Object> fieldValue = attr.getValue();
                Mapper classMapper = Mapper.of(type);
                classMapper.set(targetObject, attr.getKey(), mapper.writeValueAsString(fieldValue));
              } catch (JsonProcessingException e) {
                TraceBackService.trace(e);
              }
            });
  }

  @Override
  public <T extends Model> void fillAttrs(
      List<? extends ConfiguratorFormula> formulas,
      JsonContext jsonIndicators,
      Class<T> type,
      T targetObject) {

    fillAttrs(generateAttrMap(formulas, jsonIndicators), type, targetObject);
  }
}
