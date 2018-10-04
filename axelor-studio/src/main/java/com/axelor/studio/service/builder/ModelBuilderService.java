/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.builder;

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.ObjectViews;
import java.util.Comparator;
import java.util.List;

public class ModelBuilderService {

  private static final String NAMESPACE = "http://axelor.com/xml/ns/domain-models";

  private static final String REMOTE_SCHEMA = "domain-models_" + ObjectViews.VERSION + ".xsd";

  private static final String PACKAGE_PREFIX = "com.axelor.apps";

  public String build(MetaJsonModel model, String module) throws AxelorException {

    if (model == null || model.getFields().isEmpty() || module == null) {
      return null;
    }
    String packageName = getPackageName(module);
    //    String modelName = Inflector.getInstance().classify(model.getName());
    String modelName = model.getName();

    StringBuilder builder = new StringBuilder();
    builder.append("\t<module name=\"" + module + "\" package=\"" + packageName + "\"/>\n\n");
    builder.append("\t<entity name=\"" + modelName + "\">\n");
    builder.append(createFields(model.getFields(), module));
    builder.append("\t</entity>\n");

    return prepareXML(builder.toString());
  }

  public String build(String modelSimple, String module, String parent) throws AxelorException {

    if (modelSimple == null || module == null || parent == null) {
      return null;
    }
    String packageName = getPackageName(module);
    //    String modelName = Inflector.getInstance().classify(modelSimple);

    StringBuilder builder = new StringBuilder();
    builder.append("\t<module name=\"" + module + "\" package=\"" + packageName + "\"/>\n\n");
    builder.append("\t<entity name=\"" + modelSimple + "\" extends=\"" + parent + "\"/>\n\n");

    return prepareXML(builder.toString());
  }

  public String build(MetaModel model, List<MetaJsonField> fields, String module)
      throws AxelorException {

    if (model == null || module == null || fields.isEmpty()) {
      return null;
    }

    String packageName = model.getPackageName();
    String modelName = model.getName();

    StringBuilder builder = new StringBuilder();
    builder.append("\t<module name=\"" + module + "\" package=\"" + packageName + "\"/>\n\n");
    builder.append("\t<entity name=\"" + modelName + "\">\n");
    builder.append(createFields(fields, module));
    builder.append("\t</entity>\n");

    return prepareXML(builder.toString());
  }

  private String createFields(List<MetaJsonField> fields, String module) throws AxelorException {

    sortJsonFields(fields);

    StringBuilder fieldBuilder = new StringBuilder();

    for (MetaJsonField field : fields) {
      String type = getType(field.getType());
      if (type == null || field.getIsWkf()) {
        continue;
      }
      StringBuilder builder = new StringBuilder();
      builder.append("<");
      builder.append(type);
      builder.append(" name=\"" + field.getName() + "\"");
      builder.append(" title=\"" + field.getTitle() + "\"");
      if (type.contains("-to-")) {
        addRelationalAtts(builder, field, module);
      }
      if (field.getDefaultValue() != null) {
        builder.append(" default=\"" + field.getDefaultValue() + "\"");
      }
      if (field.getSelection() != null) {
        builder.append(" selection=\"" + field.getSelection() + "\"");
      }
      builder.append(" />");
      fieldBuilder.append("\t\t");
      fieldBuilder.append(builder.toString());
      fieldBuilder.append("\n");
    }

    return fieldBuilder.toString();
  }

  private String getType(String type) {

    switch (type) {
      case "button":
        return null;
      case "separator":
        return null;
      case "panel":
        return null;
    }

    type = type.replace("json-", "");

    return type;
  }

  private void addRelationalAtts(StringBuilder builder, MetaJsonField field, String module) {

    String target = null;
    if (field.getType().startsWith("json-")) {
      MetaJsonModel jsonModel = field.getTargetJsonModel();
      if (jsonModel != null) {
        target = getModelFullName(module, jsonModel.getName());
      }
    } else {
      target = field.getTargetModel();
    }

    if (target != null) {
      builder.append(" ref=\"" + target + "\"");
    }
  }

  private static String prepareXML(String xml) {

    StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n");
    sb.append("<domain-models")
        .append(" xmlns='")
        .append(NAMESPACE)
        .append("'")
        .append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'")
        .append(" xsi:schemaLocation='")
        .append(NAMESPACE)
        .append(" ")
        .append(NAMESPACE + "/" + REMOTE_SCHEMA)
        .append("'")
        .append(">\n\n")
        .append(xml)
        .append("\n</domain-models>");

    return sb.toString();
  }

  public String getModelFullName(String moduleName, String modelName) {

    if (moduleName == null || modelName == null) {
      return null;
    }

    //	    modelName = Inflector.getInstance().classify(modelName);

    return getPackageName(moduleName) + "." + modelName;
  }

  public String getPackageName(String moduleName) {

    if (moduleName == null) {
      return null;
    }
    moduleName = moduleName.substring(moduleName.indexOf("-") + 1, moduleName.length());
    moduleName = Inflector.getInstance().dasherize(moduleName).replaceAll("-", ".");

    return PACKAGE_PREFIX + "." + moduleName + ".db";
  }

  public void sortJsonFields(List<MetaJsonField> fields) {

    fields.sort(
        new Comparator<MetaJsonField>() {

          @Override
          public int compare(MetaJsonField field1, MetaJsonField field2) {
            return field1.getSequence().compareTo(field2.getSequence());
          }
        });
  }
}
