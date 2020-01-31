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
 * Classe Java pour LicenceWarningLevel.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="LicenceWarningLevel">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="DataExpiring"/>
 *     &lt;enumeration value="LicenceExpiring"/>
 *     &lt;enumeration value="ClicksLow"/>
 *     &lt;enumeration value="Evaluation"/>
 *     &lt;enumeration value="NoClicks"/>
 *     &lt;enumeration value="DataExpired"/>
 *     &lt;enumeration value="EvalLicenceExpired"/>
 *     &lt;enumeration value="FullLicenceExpired"/>
 *     &lt;enumeration value="LicenceNotFound"/>
 *     &lt;enumeration value="DataUnreadable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "LicenceWarningLevel")
@XmlEnum
public enum LicenceWarningLevel {
  @XmlEnumValue("None")
  NONE("None"),
  @XmlEnumValue("DataExpiring")
  DATA_EXPIRING("DataExpiring"),
  @XmlEnumValue("LicenceExpiring")
  LICENCE_EXPIRING("LicenceExpiring"),
  @XmlEnumValue("ClicksLow")
  CLICKS_LOW("ClicksLow"),
  @XmlEnumValue("Evaluation")
  EVALUATION("Evaluation"),
  @XmlEnumValue("NoClicks")
  NO_CLICKS("NoClicks"),
  @XmlEnumValue("DataExpired")
  DATA_EXPIRED("DataExpired"),
  @XmlEnumValue("EvalLicenceExpired")
  EVAL_LICENCE_EXPIRED("EvalLicenceExpired"),
  @XmlEnumValue("FullLicenceExpired")
  FULL_LICENCE_EXPIRED("FullLicenceExpired"),
  @XmlEnumValue("LicenceNotFound")
  LICENCE_NOT_FOUND("LicenceNotFound"),
  @XmlEnumValue("DataUnreadable")
  DATA_UNREADABLE("DataUnreadable");
  private final String value;

  LicenceWarningLevel(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static LicenceWarningLevel fromValue(String v) {
    for (LicenceWarningLevel c : LicenceWarningLevel.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }
}
