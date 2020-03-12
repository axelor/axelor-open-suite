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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour QAConfigType complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="QAConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IniFile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IniSection" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "QAConfigType",
  propOrder = {"iniFile", "iniSection"}
)
public class QAConfigType {

  @XmlElement(name = "IniFile")
  protected String iniFile;

  @XmlElement(name = "IniSection")
  protected String iniSection;

  /**
   * Obtient la valeur de la propriété iniFile.
   *
   * @return possible object is {@link String }
   */
  public String getIniFile() {
    return iniFile;
  }

  /**
   * Définit la valeur de la propriété iniFile.
   *
   * @param value allowed object is {@link String }
   */
  public void setIniFile(String value) {
    this.iniFile = value;
  }

  /**
   * Obtient la valeur de la propriété iniSection.
   *
   * @return possible object is {@link String }
   */
  public String getIniSection() {
    return iniSection;
  }

  /**
   * Définit la valeur de la propriété iniSection.
   *
   * @param value allowed object is {@link String }
   */
  public void setIniSection(String value) {
    this.iniSection = value;
  }
}
