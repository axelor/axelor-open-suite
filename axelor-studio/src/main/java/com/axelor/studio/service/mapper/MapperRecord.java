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

import java.util.ArrayList;
import java.util.List;

public class MapperRecord {

  private String targetModel = null;

  private String sourceModel = null;

  private boolean newRecord = true;

  private boolean createVariable = false;

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

  public boolean getCreateVariable() {
    return createVariable;
  }

  public void setCreateVariable(boolean createVariable) {
    this.createVariable = createVariable;
  }

  public String toScript() {
    StringBuilder stb = new StringBuilder();
    if (newRecord) {
      stb.append("def rec = $ctx.create('" + targetModel + "')\n");
    } else {
      stb.append(
          "def rec = "
              + targetModel.substring(0, 1).toLowerCase()
              + targetModel.substring(1)
              + "\n");
    }

    if (sourceModel != null) {
      stb.append(
          "def src = "
              + sourceModel.substring(0, 1).toLowerCase()
              + sourceModel.substring(1)
              + "\n");
    }

    if (fields != null) {
      for (MapperField field : fields) {
        stb.append(field.toScript("rec") + "\n");
      }
    }

    if (createVariable) {
      stb.append("$ctx.createVariable($ctx.save(rec), execution)");
    } else {
      stb.append("return $ctx.save(rec)");
    }
    return stb.toString();
  }
}
