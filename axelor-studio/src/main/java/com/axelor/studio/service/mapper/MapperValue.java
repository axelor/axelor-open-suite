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
package com.axelor.studio.service.mapper;

import groovy.json.StringEscapeUtils;
import java.util.ArrayList;
import java.util.List;

public class MapperValue {

  private MapperSelected selected = null;

  private MapperField parentField = null;

  private String from = null;

  private List<MapperField> fields = new ArrayList<MapperField>();

  public MapperSelected getSelected() {
    return selected;
  }

  public void setSelected(MapperSelected selected) {
    this.selected = selected;
  }

  public List<MapperField> getFields() {
    return fields;
  }

  public void setFields(List<MapperField> fields) {
    this.fields = fields;
  }

  public String toScript(MapperField parentField) {

    this.parentField = parentField;

    StringBuilder stb = new StringBuilder();

    switch (from) {
      case "none":
        processNone(stb);
        break;
      case "context":
        processContext(stb);
        break;
      case "self":
        processSelf(stb);
        break;
      case "source":
        processSource(stb);
        break;
      default:
        break;
    }

    for (MapperField field : fields) {
      stb.append("?" + field.toScript(parentField.getField()) + "\n");
    }

    return stb.toString();
  }

  private String getSelectedScript() {
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

  private void processNone(StringBuilder stb) {

    String target = parentField.getTarget();
    String jsonModel = parentField.getJsonModel();

    String value = getSelectedScript();

    if (target != null || jsonModel != null) {

      String model = jsonModel != null ? jsonModel : target;
      model = "'" + model + "'";

      if (value != null) {
        stb.append("$ctx.filterOne(" + model + "," + prepareQuery(jsonModel, value) + ")");
      } else {
        stb.append("$ctx.create(" + model + ")");
      }

      if (fields.isEmpty()) {
        stb.append("?.getTarget()");
      }

    } else {
      stb.append(getTypedValue(value, false));
    }
  }

  private String prepareQuery(String jsonModel, String fieldValue) {

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

  private String getTypedValue(String value, boolean isString) {

    String type = parentField.getType();

    if (isString) {
      type = "STRING";
    }

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

  private void processContext(StringBuilder stb) {

    String value = getSelectedScript();

    String parentType = parentField.getType();
    if ((fields == null || fields.isEmpty())
        && (parentType.equals("MANY_TO_ONE") || parentType.equals("many-to-one"))) {
      if (value != null && !value.endsWith(".id")) {
        value += "?.id";
      }
      stb.append("$ctx.find('" + parentField.getTarget() + "'," + value + ")?.getTarget()");
    } else {
      stb.append(value);
    }
  }

  private void processSelf(StringBuilder stb) {

    stb.append(getSelectedScript());
  }

  private void processSource(StringBuilder stb) {

    if ((fields == null || fields.isEmpty()) && parentField.getType().equals("MANY_TO_ONE")) {
      stb.append(
          "$ctx.find('"
              + parentField.getTarget()
              + "',"
              + "src."
              + getSelectedScript()
              + "?.id)?.getTarget()");
    } else {
      stb.append("src." + getSelectedScript());
    }
  }
}
