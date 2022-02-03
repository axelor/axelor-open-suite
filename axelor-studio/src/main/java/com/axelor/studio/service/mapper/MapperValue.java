/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import groovy.json.StringEscapeUtils;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapperValue {

  public static final List<String> MANY_TO_ONE_TYPE =
      Arrays.asList(
          new String[] {"MANY_TO_ONE", "many-to-one", "json-many-to-one", "json-one-to-one"});

  private MapperSelected selected = null;

  private MapperField parentField = null;

  private String from = null;

  public MapperSelected getSelected() {
    return selected;
  }

  public void setSelected(MapperSelected selected) {
    this.selected = selected;
  }

  public MapperField getParentField() {
    return parentField;
  }

  public void setParentField(MapperField parentField) {
    this.parentField = parentField;
  }

  public String toScript(MapperField parentField) {

    setParentField(parentField);

    StringBuilder stb = new StringBuilder();

    switch (from) {
      case "none":
        mapNone(stb);
        break;
      case "context":
        mapContext(stb);
        break;
      case "self":
        mapSelf(stb);
        break;
      case "source":
        mapSource(stb);
        break;
      default:
        break;
    }

    return stb.toString();
  }

  protected String getSelectedScript() {
    if (selected != null) {
      return selected.toScript();
    }

    return null;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  protected void mapNone(StringBuilder stb) {

    String target = parentField.getTarget();
    String targetJsonModel = parentField.getJsonModel();

    String value = getSelectedScript();

    if (target != null || targetJsonModel != null) {

      String model = targetJsonModel != null ? targetJsonModel : target;
      model = "'" + model + "'";

      if (value != null) {
        stb.append("$ctx.filterOne(" + model + "," + prepareQuery(targetJsonModel, value) + ")");
      } else {
        stb.append("$ctx.create(" + model + ")");
      }

      stb.append("?.getTarget()");

    } else {
      stb.append(getTypedValue(value, false));
    }
  }

  protected String prepareQuery(String jsonModel, String fieldValue) {

    String[] queryParts = fieldValue.split(",");

    String query = "self.";

    if (jsonModel != null) {
      query += "attrs.";
    }

    String param = queryParts[1];
    param = getTypedValue(param, true);

    query = "'" + query + queryParts[0] + " = ?1'," + param;

    return query;
  }

  protected String getTypedValue(String value, boolean isString) {

    String type = parentField.getType();

    if (isString) {
      type = "STRING";
    }
    type = type.toUpperCase();

    switch (type) {
      case "STRING":
        value = "'" + StringEscapeUtils.escapeJavaScript(value) + "'";
        break;
      case "DATE":
        value =
            "LocalDate.parse('"
                + value
                + "', java.time.format.DateTimeFormatter.ofPattern('dd/MM/yyyy'))";
        break;
      case "DATETIME":
        value =
            "LocalDateTime.parse('"
                + value
                + "', java.time.format.DateTimeFormatter.ofPattern('dd/MM/yyyy HH:mm'))";
        break;
    }

    return value;
  }

  protected void mapContext(StringBuilder stb) {

    String value = getSelectedScript();

    if (MANY_TO_ONE_TYPE.contains(parentField.getType())) {
      if (value != null && !value.endsWith(".id")) {
        value += "?.id";
      }
      stb.append("$ctx.find('" + parentField.getTarget() + "'," + value + ")?.getTarget()");
    } else {
      stb.append(value);
    }
  }

  private void mapSelf(StringBuilder stb) {

    stb.append(getSelectedScript());
  }

  protected void mapSource(StringBuilder stb) {

    String selected = "src." + getSelectedScript();
    if (MANY_TO_ONE_TYPE.contains(parentField.getType())) {
      if (selected.equals("src.SOURCE")) {
        selected = "src";
      }
      stb.append("$ctx.find('" + parentField.getTarget() + "'," + selected + "?.id)?.getTarget()");
    } else {
      stb.append(selected);
    }
  }
}
