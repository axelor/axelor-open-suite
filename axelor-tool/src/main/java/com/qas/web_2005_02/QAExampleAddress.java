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
 * Classe Java pour QAExampleAddress complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="QAExampleAddress">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Address" type="{http://www.qas.com/web-2005-02}QAAddressType"/>
 *         &lt;element name="Comment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "QAExampleAddress",
  propOrder = {"address", "comment"}
)
public class QAExampleAddress {

  @XmlElement(name = "Address", required = true)
  protected QAAddressType address;

  @XmlElement(name = "Comment", required = true)
  protected String comment;

  /**
   * Obtient la valeur de la propriété address.
   *
   * @return possible object is {@link QAAddressType }
   */
  public QAAddressType getAddress() {
    return address;
  }

  /**
   * Définit la valeur de la propriété address.
   *
   * @param value allowed object is {@link QAAddressType }
   */
  public void setAddress(QAAddressType value) {
    this.address = value;
  }

  /**
   * Obtient la valeur de la propriété comment.
   *
   * @return possible object is {@link String }
   */
  public String getComment() {
    return comment;
  }

  /**
   * Définit la valeur de la propriété comment.
   *
   * @param value allowed object is {@link String }
   */
  public void setComment(String value) {
    this.comment = value;
  }
}
