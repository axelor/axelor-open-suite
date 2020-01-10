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
 * Classe Java pour VerifyLevelType.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="VerifyLevelType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Verified"/>
 *     &lt;enumeration value="InteractionRequired"/>
 *     &lt;enumeration value="PremisesPartial"/>
 *     &lt;enumeration value="StreetPartial"/>
 *     &lt;enumeration value="Multiple"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "VerifyLevelType")
@XmlEnum
public enum VerifyLevelType {
  @XmlEnumValue("None")
  NONE("None"),
  @XmlEnumValue("Verified")
  VERIFIED("Verified"),
  @XmlEnumValue("InteractionRequired")
  INTERACTION_REQUIRED("InteractionRequired"),
  @XmlEnumValue("PremisesPartial")
  PREMISES_PARTIAL("PremisesPartial"),
  @XmlEnumValue("StreetPartial")
  STREET_PARTIAL("StreetPartial"),
  @XmlEnumValue("Multiple")
  MULTIPLE("Multiple");
  private final String value;

  VerifyLevelType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static VerifyLevelType fromValue(String v) {
    for (VerifyLevelType c : VerifyLevelType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }
}
