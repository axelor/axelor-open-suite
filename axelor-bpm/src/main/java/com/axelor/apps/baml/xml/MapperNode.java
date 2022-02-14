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

import com.axelor.apps.tool.StringTool;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class MapperNode extends BaseNode {

  @XmlElement(name = "script", type = String.class)
  private String script;

  @XmlElement(name = "scriptMeta", type = String.class)
  private String scriptMeta;

  @XmlAttribute(name = "targetField")
  private String targetField;

  @XmlAttribute(name = "sourceField")
  private String sourceField;

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public String getScriptMeta() {
    return scriptMeta;
  }

  public void setScriptMeta(String scriptMeta) {
    this.scriptMeta = scriptMeta;
  }

  public String getTargetField() {
    return targetField;
  }

  public void setTargetField(String targetField) {
    this.targetField = targetField;
  }

  public String getSourceField() {
    return sourceField;
  }

  public void setSourceField(String sourceField) {
    this.sourceField = sourceField;
  }

  @Override
  public String toCode(boolean dynamic) {

    StringBuilder codeBuilder = new StringBuilder();
    if (script == null) {
      return codeBuilder.toString();
    }

    if (script.substring(script.lastIndexOf("\n") + 1).startsWith("return")) {
      String target = StringTool.toFirstLower(targetField);
      codeBuilder.append("def " + target + " = {\n" + script + "\n}()\n");
    } else {
      codeBuilder.append("\n" + script + "\n");
    }

    return codeBuilder.toString();
  }
}
