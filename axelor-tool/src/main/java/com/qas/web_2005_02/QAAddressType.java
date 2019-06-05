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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour QAAddressType complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="QAAddressType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AddressLine" type="{http://www.qas.com/web-2005-02}AddressLineType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Overflow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Truncated" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "QAAddressType",
  propOrder = {"addressLine"}
)
public class QAAddressType {

  @XmlElement(name = "AddressLine", required = true)
  protected List<AddressLineType> addressLine;

  @XmlAttribute(name = "Overflow")
  protected Boolean overflow;

  @XmlAttribute(name = "Truncated")
  protected Boolean truncated;

  /**
   * Gets the value of the addressLine property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the addressLine property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getAddressLine().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link AddressLineType }
   */
  public List<AddressLineType> getAddressLine() {
    if (addressLine == null) {
      addressLine = new ArrayList<AddressLineType>();
    }
    return this.addressLine;
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
