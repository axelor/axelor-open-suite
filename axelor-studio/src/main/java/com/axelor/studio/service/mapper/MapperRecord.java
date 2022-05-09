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

import com.axelor.apps.tool.StringTool;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapperRecord {

  private String targetModel = null;

  private String sourceModel = null;

  private boolean newRecord = true;

  private boolean savedRecord = false;

  private String targetVariable = "rec";

  private StringBuilder scriptBuilder = new StringBuilder();

  private List<MapperField> fields = new ArrayList<MapperField>();

  public String getTargetModel() {
    return targetModel;
  }

  public void setTargetModel(String targetModel) {
    this.targetModel = targetModel;
  }

  public boolean isNewRecord() {
    return newRecord;
  }

  public void setNewRecord(boolean newRecord) {
    this.newRecord = newRecord;
  }

  public boolean isSavedRecord() {
    return savedRecord;
  }

  public void setSavedRecord(boolean savedRecord) {
    this.savedRecord = savedRecord;
  }

  public List<MapperField> getFields() {
    return fields;
  }

  public void setFields(List<MapperField> fields) {
    this.fields = fields;
  }

  public String getSourceModel() {
    return sourceModel;
  }

  public void setSourceModel(String sourceModel) {
    this.sourceModel = sourceModel;
  }

  public StringBuilder getScriptBuilder() {
    return this.scriptBuilder;
  }

  public String getTargetVariable() {
    return targetVariable;
  }

  public void setTargetVariable(String targetVariable) {
    this.targetVariable = targetVariable;
  }

  public String toScript() {

    scriptBuilder = new StringBuilder();

    addTarget();

    if (!Strings.isNullOrEmpty(sourceModel)) {

      addSource();
    }

    addFields();

    addReturn();

    return scriptBuilder.toString();
  }

  public void addSource() {

    String src = StringTool.toFirstLower(sourceModel);

    src = "def src = " + src + "\n";

    scriptBuilder.append(src);
  }

  public StringBuilder addTarget() {

    if (newRecord) {
      scriptBuilder.append("def " + targetVariable + " = $ctx.create('" + targetModel + "')\n");
    } else if (savedRecord) {
      scriptBuilder.append(
          "def "
              + targetVariable
              + " = $ctx.find('"
              + targetModel
              + "',"
              + StringTool.toFirstLower(targetModel)
              + "Id)\n");
    } else {
      targetVariable = StringTool.toFirstLower(targetModel);
    }

    return scriptBuilder;
  }

  public void addFields() {

    if (fields != null) {
      for (MapperField field : fields) {
        scriptBuilder.append(field.toScript(targetVariable) + "\n");
      }
    }
  }

  public void addReturn() {

    if (newRecord || savedRecord) {
      scriptBuilder.append("return $ctx.save(" + targetVariable + ")");
    }
  }
}
