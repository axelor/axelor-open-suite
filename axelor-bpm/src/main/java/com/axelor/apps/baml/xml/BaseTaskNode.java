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
import org.apache.commons.lang3.StringEscapeUtils;

@XmlType
public abstract class BaseTaskNode extends BaseNode {

  @XmlAttribute(name = "target")
  private String target;

  @XmlAttribute(name = "expression")
  private String expression;

  public String getTarget() {
    return target;
  }

  public String getExpression() {
    if (expression == null) {
      return null;
    }
    return StringEscapeUtils.unescapeXml(expression);
  }
}
