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

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class ProcessActionNode extends BaseNode {

  @XmlElements(
      value = {
        @XmlElement(name = "new-record", type = NewRecordNode.class),
        @XmlElement(name = "mapper", type = MapperNode.class),
        @XmlElement(name = "loop", type = LoopNode.class),
        @XmlElement(name = "conditional", type = ConditionalNode.class),
        @XmlElement(name = "query", type = QueryNode.class),
        @XmlElement(name = "function", type = FunctionNode.class)
      })
  private List<BaseNode> nodes;

  @XmlAttribute(name = "sourceModel")
  private String sourceModel;

  @XmlAttribute(name = "targetModel")
  private String targetModel;

  @XmlAttribute(name = "staticCompile")
  private boolean staticCompile;

  public List<BaseNode> getNodes() {
    return nodes;
  }

  public String getSourceModel() {
    return sourceModel;
  }

  public String getTargetModel() {
    return targetModel;
  }

  public boolean getStaticCompile() {
    return staticCompile;
  }

  @Override
  public String toCode(boolean dynamic) {

    if (nodes == null) {
      return "";
    }

    StringBuilder codeBuilder = new StringBuilder();
    for (BaseNode baseNode : nodes) {
      codeBuilder.append(baseNode.toCode(dynamic));
    }

    return codeBuilder.toString();
  }
}
