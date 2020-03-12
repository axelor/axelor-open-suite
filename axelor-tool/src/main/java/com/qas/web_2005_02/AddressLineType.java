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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour AddressLineType complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="AddressLineType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Label" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Line" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="LineContent" type="{http://www.qas.com/web-2005-02}LineContentType" default="Address" />
 *       &lt;attribute name="Overflow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Truncated" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "AddressLineType",
  propOrder = {"label", "line"}
)
public class AddressLineType {

  @XmlElement(name = "Label")
  protected String label;

  @XmlElement(name = "Line")
  protected String line;

  @XmlAttribute(name = "LineContent")
  protected LineContentType lineContent;

  @XmlAttribute(name = "Overflow")
  protected Boolean overflow;

  @XmlAttribute(name = "Truncated")
  protected Boolean truncated;

  /**
   * Obtient la valeur de la propriété label.
   *
   * @return possible object is {@link String }
   */
  public String getLabel() {
    return label;
  }

  /**
   * Définit la valeur de la propriété label.
   *
   * @param value allowed object is {@link String }
   */
  public void setLabel(String value) {
    this.label = value;
  }

  /**
   * Obtient la valeur de la propriété line.
   *
   * @return possible object is {@link String }
   */
  public String getLine() {
    return line;
  }

  /**
   * Définit la valeur de la propriété line.
   *
   * @param value allowed object is {@link String }
   */
  public void setLine(String value) {
    this.line = value;
  }

  /**
   * Obtient la valeur de la propriété lineContent.
   *
   * @return possible object is {@link LineContentType }
   */
  public LineContentType getLineContent() {
    if (lineContent == null) {
      return LineContentType.ADDRESS;
    } else {
      return lineContent;
    }
  }

  /**
   * Définit la valeur de la propriété lineContent.
   *
   * @param value allowed object is {@link LineContentType }
   */
  public void setLineContent(LineContentType value) {
    this.lineContent = value;
  }

  /**
   * Obtient la valeur de la propriété overflow.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isOverflow() {
    if (overflow == null) {
      return false;
    } else {
      return overflow;
    }
  }

  /**
   * Définit la valeur de la propriété overflow.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setOverflow(Boolean value) {
    this.overflow = value;
  }

  /**
   * Obtient la valeur de la propriété truncated.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isTruncated() {
    if (truncated == null) {
      return false;
    } else {
      return truncated;
    }
  }

  /**
   * Définit la valeur de la propriété truncated.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setTruncated(Boolean value) {
    this.truncated = value;
  }
}
