/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour PromptSetType.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="PromptSetType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OneLine"/>
 *     &lt;enumeration value="Default"/>
 *     &lt;enumeration value="Generic"/>
 *     &lt;enumeration value="Optimal"/>
 *     &lt;enumeration value="Alternate"/>
 *     &lt;enumeration value="Alternate2"/>
 *     &lt;enumeration value="Alternate3"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "PromptSetType")
@XmlEnum
public enum PromptSetType {
  @XmlEnumValue("OneLine")
  ONE_LINE("OneLine"),
  @XmlEnumValue("Default")
  DEFAULT("Default"),
  @XmlEnumValue("Generic")
  GENERIC("Generic"),
  @XmlEnumValue("Optimal")
  OPTIMAL("Optimal"),
  @XmlEnumValue("Alternate")
  ALTERNATE("Alternate"),
  @XmlEnumValue("Alternate2")
  ALTERNATE_2("Alternate2"),
  @XmlEnumValue("Alternate3")
  ALTERNATE_3("Alternate3");
  private final String value;

  PromptSetType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static PromptSetType fromValue(String v) {
    for (PromptSetType c : PromptSetType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }
}
