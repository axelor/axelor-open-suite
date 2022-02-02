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
package com.axelor.apps.bpm.mapper;

import com.axelor.apps.tool.StringTool;
import com.axelor.studio.service.mapper.MapperRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class BpmMapperRecord extends MapperRecord {

  private String processId = null;

  private boolean createVariable = false;

  @JsonProperty("fields")
  private List<BpmMapperField> bpmMapperFields = new ArrayList<BpmMapperField>();

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public boolean getCreateVariable() {
    return createVariable;
  }

  public void setCreateVariable(boolean createVariable) {
    this.createVariable = createVariable;
  }

  public List<BpmMapperField> getBpmMapperFields() {
    return bpmMapperFields;
  }

  public void setBpmMapperFields(List<BpmMapperField> bpmMapperFields) {
    this.bpmMapperFields = bpmMapperFields;
  }

  @Override
  public void addReturn() {

    StringBuilder scriptBuilder = getScriptBuilder();

    if (createVariable) {
      scriptBuilder.append(
          "$ctx.createVariable($ctx.save(" + getTargetVariable() + "), execution)");
    } else if (isSavedRecord() || isNewRecord()) {
      scriptBuilder.append("return $ctx.save(" + getTargetVariable() + ")");
    }
  }

  @Override
  public void addSource() {

    StringBuilder scriptBuilder = getScriptBuilder();

    String src = StringTool.toFirstLower(getSourceModel());

    if (processId != null) {
      src = "$ctx.getVariable(" + processId + ",'" + src + "')";
    }

    src = "def src = " + src + "\n";

    scriptBuilder.append(src);
  }

  @Override
  public void addFields() {

    if (bpmMapperFields != null) {
      for (BpmMapperField field : bpmMapperFields) {
        getScriptBuilder().append(field.toScript(getTargetVariable()) + "\n");
      }
    }
  }
}
