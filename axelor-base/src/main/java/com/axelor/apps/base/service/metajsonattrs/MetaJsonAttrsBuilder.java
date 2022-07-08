package com.axelor.apps.base.service.metajsonattrs;

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaJsonAttrsBuilder {
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Map<String, Object> attrs;

  public MetaJsonAttrsBuilder(String attrs)
      throws JsonParseException, JsonMappingException, IOException {

    if (attrs == null || attrs.isEmpty()) {
      this.attrs = new HashMap<>();
    } else {
      this.attrs = readValue(attrs);
    }
  }

  public MetaJsonAttrsBuilder() {
    this.attrs = new HashMap<>();
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
