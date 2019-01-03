/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour anonymous complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Layout" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Moniker" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="QAConfig" type="{http://www.qas.com/web-2005-02}QAConfigType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "",
  propOrder = {"layout", "moniker", "qaConfig"}
)
@XmlRootElement(name = "QAGetAddress")
public class QAGetAddress {

  @XmlElement(name = "Layout", required = true)
  protected String layout;

  @XmlElement(name = "Moniker", required = true)
  protected String moniker;

  @XmlElement(name = "QAConfig")
  protected QAConfigType qaConfig;

  /**
   * Obtient la valeur de la propriété layout.
   *
   * @return possible object is {@link String }
   */
  public String getLayout() {
    return layout;
  }

  /**
   * Définit la valeur de la propriété layout.
   *
   * @param value allowed object is {@link String }
   */
  public void setLayout(String value) {
    this.layout = value;
  }

  /**
   * Obtient la valeur de la propriété moniker.
   *
   * @return possible object is {@link String }
   */
  public String getMoniker() {
    return moniker;
  }

  /**
   * Définit la valeur de la propriété moniker.
   *
   * @param value allowed object is {@link String }
   */
  public void setMoniker(String value) {
    this.moniker = value;
  }

  /**
   * Obtient la valeur de la propriété qaConfig.
   *
   * @return possible object is {@link QAConfigType }
   */
  public QAConfigType getQAConfig() {
    return qaConfig;
  }

  /**
   * Définit la valeur de la propriété qaConfig.
   *
   * @param value allowed object is {@link QAConfigType }
   */
  public void setQAConfig(QAConfigType value) {
    this.qaConfig = value;
  }
}
