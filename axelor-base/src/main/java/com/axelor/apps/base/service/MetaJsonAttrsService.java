package com.axelor.apps.base.service;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.tool.MetaTool;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaJsonField;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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

public class MetaJsonAttrsService {

  public static class MetaJsonAttrsBuilder {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<String, Object> attrs;

    public MetaJsonAttrsBuilder(String attrs)
        throws JsonParseException, JsonMappingException, IOException {

      if (attrs == null || attrs.isEmpty()) {
        this.attrs = new HashMap<String, Object>();
      } else {
        this.attrs = readValue(attrs);
      }
    }

    public MetaJsonAttrsBuilder() {
      this.attrs = new HashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readValue(String attrs)
        throws JsonParseException, JsonMappingException, IOException {
      logger.debug("Parsing json {} into map", attrs);
      ObjectMapper mapper = new ObjectMapper();

      return mapper.readValue(attrs, Map.class);
    }

    public MetaJsonAttrsBuilder putValue(MetaJsonField metaJsonField, Object value)
        throws AxelorException {
      logger.debug("Putting {} in {}", value, metaJsonField);
      Objects.requireNonNull(value);
      Map.Entry<String, Object> entry = MetaJsonAttrsAdapter.adaptValueForMap(metaJsonField, value);

      this.attrs.put(entry.getKey(), entry.getValue());

      return this;
    }

    public String build() throws JsonProcessingException {
      logger.debug("Building map {} into string json", this.attrs);
      ObjectMapper mapper = new ObjectMapper();

      return mapper.writeValueAsString(attrs);
    }
  }

  static class MetaJsonAttrsAdapter {
    private static final Logger logger =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static Map.Entry<String, Object> adaptValueForMap(MetaJsonField metaJsonField, Object value)
        throws AxelorException {

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
              I18n.get(IExceptionMessage.META_JSON_TYPE_NOT_MANAGED),
              fieldName);
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.META_JSON_TYPE_NO_MATCH_OBJECT_VALUE),
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
}
