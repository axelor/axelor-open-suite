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
package com.axelor.apps.baml.xml;

import com.axelor.meta.db.MetaJsonRecord;
import com.google.common.base.Strings;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class QueryNode extends BaseTaskNode {

  @XmlAttribute(name = "returnType")
  private ReturnType returnType;

  @XmlAttribute(name = "model")
  private String model;

  @XmlAttribute(name = "isJson")
  private boolean isJson;

  public ReturnType getReturnType() {
    return returnType;
  }

  public String getModel() {
    return model;
  }

  public boolean getIsJson() {
    return isJson;
  }

  @Override
  public String toCode(boolean dynamic) {

    StringBuilder codeBuilder = new StringBuilder();

    String model = getModel();

    if (dynamic) {
      addDynamicQuery(codeBuilder, model);
    } else {
      addQuery(codeBuilder, model);
    }

    String target = getTarget();

    if (!target.contains(".")) {
      target = "def " + target;
    }

    return "\n" + target + " = " + codeBuilder.toString();
  }

  private void addQuery(StringBuilder codeBuilder, String model) {

    String filter = getExpression();

    StringBuilder params = new StringBuilder();

    if (isJson) {
      if (!Strings.isNullOrEmpty(filter)) {
        filter = "(" + filter + ") AND ";
      }
      filter += "self.jsonModel = :jsonModel";
      params.append(".bind('jsonModel', '" + model + "')");
      model = MetaJsonRecord.class.getName();
    }

    codeBuilder.append("com.axelor.db.Query.of(" + model + ")");

    if (!Strings.isNullOrEmpty(filter)) {
      codeBuilder.append(".filter(" + filter + ")");
      codeBuilder.append(params.toString());
    }

    if (returnType.equals(ReturnType.SINGLE)) {
      codeBuilder.append(".fetchOne()\n");
    } else {
      codeBuilder.append(".fetch()\n");
    }
  }

  private void addDynamicQuery(StringBuilder codeBuilder, String model) {

    String filter = getExpression();

    if (returnType.equals(ReturnType.SINGLE)) {
      codeBuilder.append("WkfContextHelper.filterOne('" + model + "'," + filter + ")\n");
    } else {
      codeBuilder.append("WkfContextHelper.filter('" + model + "'," + filter + ")\n");
    }
  }
}
