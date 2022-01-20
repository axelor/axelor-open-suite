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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class LoopNode extends ProcessActionNode {

  @XmlAttribute(name = "target")
  private String target;

  @XmlAttribute(name = "expression")
  private String expression;

  public String getTarget() {
    return target;
  }

  public String getExpression() {
    return expression;
  }

  @Override
  public String toCode(boolean dynamic) {

    StringBuilder codeBuilder = new StringBuilder();

    if (target != null && expression != null) {
      codeBuilder.append("\nfor (" + target + " in " + expression + "){\n");
      codeBuilder.append(super.toCode(dynamic));
      codeBuilder.append("\n}");
    }

    return codeBuilder.toString();
  }
}
