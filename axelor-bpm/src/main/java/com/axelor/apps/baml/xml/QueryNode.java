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
package com.axelor.apps.baml.xml;

import com.axelor.meta.db.MetaJsonRecord;
import com.google.common.base.Strings;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class QueryNode extends BaseTaskNode {

  @XmlElements({@XmlElement(name = "parameter", type = QueryParameterNode.class)})
  private List<QueryParameterNode> parameters;

  @XmlAttribute(name = "returnType")
  private ReturnType returnType;

  @XmlAttribute(name = "model")
  private String model;

  @XmlAttribute(name = "isJson")
  private boolean isJson;

  public List<QueryParameterNode> getParameters() {
    return parameters;
  }

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

    StringBuilder params = new StringBuilder();
    if (getParameters() != null) {
      if (dynamic) {
        params.append("[");
      }
      for (QueryParameterNode node : parameters) {
        params.append(node.toCode(dynamic));
      }
      if (dynamic) {
        params.deleteCharAt(params.length() - 1);
        params.append("]");
      }
    }

    if (dynamic) {
      addDynamicQuery(codeBuilder, model, params);
    } else {
      addQuery(codeBuilder, model, params);
    }

    String target = getTarget();

    if (!target.contains(".")) {
      target = "def " + target;
    }

    return "\n" + target + " = " + codeBuilder.toString();
  }

  private void addQuery(StringBuilder codeBuilder, String model, StringBuilder params) {

    String filter = getExpression();
    if (isJson) {
      filter += "(" + filter + ") AND self.jsonModel = :jsonModel";
      params.append(".bind('jsonModel', '" + model + "')");
      model = MetaJsonRecord.class.getName();
    }

    codeBuilder.append("com.axelor.db.Query.of(" + model + ")");

    if (!Strings.isNullOrEmpty(filter)) {
      codeBuilder.append(".filter(\"" + filter + "\")");
      codeBuilder.append(params.toString());
    }

    if (returnType.equals(ReturnType.SINGLE)) {
      codeBuilder.append(".fetchOne()\n");
    } else {
      codeBuilder.append(".fetch()\n");
    }
  }

  private void addDynamicQuery(StringBuilder codeBuilder, String model, StringBuilder params) {

    String filter = "\"" + getExpression() + "\"";

    String parameters = "";
    if (params.length() != 0) {
      parameters = "," + params.toString();
    }

    if (returnType.equals(ReturnType.SINGLE)) {
      codeBuilder.append(
          "WkfContextHelper.filterOne('" + model + "'," + filter + parameters + ")\n");
    } else {
      codeBuilder.append("WkfContextHelper.filter('" + model + "'," + filter + parameters + ")\n");
    }
  }
}
