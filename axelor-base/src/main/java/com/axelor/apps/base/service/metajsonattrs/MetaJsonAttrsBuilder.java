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
