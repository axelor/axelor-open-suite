package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.tool.MetaTool;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.JsonContext;
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

  @Override
  public Map<String, Map<String, Object>> generateAttrMap(
      List<? extends ConfiguratorFormula> formulas, JsonContext jsonIndicators) {
    // This map keys are attrs fields
    // This map values are a map<namefield, object> associated to the attr field
    // The purpose of this map is to compute it, in order to fill attr fields of Object Product
    HashMap<String, Map<String, Object>> attrValueMap = new HashMap<>();
    // Keys to remove from map, because we don't need them afterward
    List<String> keysToRemove = new ArrayList<>();
    jsonIndicators.entrySet().stream()
        .map(entry -> entry.getKey())
        .filter(fullName -> fullName.contains("$"))
        .forEach(
            fullName -> {
              formulas.forEach(
                  formula -> {
                    String[] nameFieldInfo = fullName.split("[\\$_]");
                    String attrName = nameFieldInfo[0];
                    String fieldName = nameFieldInfo[1];
                    if (formula.getMetaJsonField() != null
                        && attrName.equals(formula.getMetaField().getName())
                        && fieldName.equals(formula.getMetaJsonField().getName())) {
                      putFieldValueInMap(
                          fieldName,
                          jsonIndicators.get(fullName),
                          attrName,
                          formula.getMetaJsonField(),
                          attrValueMap);
                      keysToRemove.add(fullName);
                    }
                  });
            });

    jsonIndicators.entrySet().removeIf(entry -> keysToRemove.contains(entry.getKey()));
    return attrValueMap;
  }

  @Override
  public Map<String, Object> modelToJson(Model model) {
    final Map<String, Object> manyToOneObject = new HashMap<>();
    manyToOneObject.put("id", model.getId());
    return manyToOneObject;
  }

  @Override
  public Map<String, Object> objectMapToJson(Map<String, Object> map) {

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

    if (!attrValueMap.containsKey(attrName)) {
      attrValueMap.put(attrName, new HashMap<>());
    }
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
    } catch (AxelorException | IllegalArgumentException | SecurityException e) {

      TraceBackService.trace(e);
    }
    return new AbstractMap.SimpleEntry<>(nameField, object);
  }

  @Override
  public <T extends Model> void fillAttrs(
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
  public List<MetaJsonField> filterIndicators(
      Configurator configurator, List<MetaJsonField> indicators) {

    List<ConfiguratorFormula> formulas = new ArrayList<>();
    formulas.addAll(configurator.getConfiguratorCreator().getConfiguratorProductFormulaList());
    formulas.addAll(configurator.getConfiguratorCreator().getConfiguratorSOLineFormulaList());

    return indicators.stream()
        .filter(metaJsonField -> !isOneToManyNotAttr(formulas, metaJsonField))
        .collect(Collectors.toList());
  }

  protected Boolean isOneToManyNotAttr(
      List<ConfiguratorFormula> formulas, MetaJsonField metaJsonField) {

    return "one-to-many".equals(metaJsonField.getType()) && !metaJsonField.getName().contains("$");
  }
}
