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
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element name="QAPicklist" type="{http://www.qas.com/web-2005-02}QAPicklistType" minOccurs="0"/>
 *         &lt;element name="QAAddress" type="{http://www.qas.com/web-2005-02}QAAddressType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="VerifyLevel" type="{http://www.qas.com/web-2005-02}VerifyLevelType" default="None" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "",
  propOrder = {"qaPicklist", "qaAddress"}
)
@XmlRootElement(name = "QASearchResult")
public class QASearchResult {

  @XmlElement(name = "QAPicklist")
  protected QAPicklistType qaPicklist;

  @XmlElement(name = "QAAddress")
  protected QAAddressType qaAddress;

  @XmlAttribute(name = "VerifyLevel")
  protected VerifyLevelType verifyLevel;

  /**
   * Obtient la valeur de la propriété qaPicklist.
   *
   * @return possible object is {@link QAPicklistType }
   */
  public QAPicklistType getQAPicklist() {
    return qaPicklist;
  }

  /**
   * Définit la valeur de la propriété qaPicklist.
   *
   * @param value allowed object is {@link QAPicklistType }
   */
  public void setQAPicklist(QAPicklistType value) {
    this.qaPicklist = value;
  }

  /**
   * Obtient la valeur de la propriété qaAddress.
   *
   * @return possible object is {@link QAAddressType }
   */
  public QAAddressType getQAAddress() {
    return qaAddress;
  }

  /**
   * Définit la valeur de la propriété qaAddress.
   *
   * @param value allowed object is {@link QAAddressType }
   */
  public void setQAAddress(QAAddressType value) {
    this.qaAddress = value;
  }

  /**
   * Obtient la valeur de la propriété verifyLevel.
   *
   * @return possible object is {@link VerifyLevelType }
   */
  public VerifyLevelType getVerifyLevel() {
    if (verifyLevel == null) {
      return VerifyLevelType.NONE;
    } else {
      return verifyLevel;
    }
  }

  /**
   * Définit la valeur de la propriété verifyLevel.
   *
   * @param value allowed object is {@link VerifyLevelType }
   */
  public void setVerifyLevel(VerifyLevelType value) {
    this.verifyLevel = value;
  }
}
