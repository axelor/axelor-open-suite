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
 *         &lt;element name="ErrorCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ErrorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "",
  propOrder = {"errorCode", "errorMessage"}
)
@XmlRootElement(name = "QAFault")
public class QAFault {

  @XmlElement(name = "ErrorCode", required = true)
  protected String errorCode;

  @XmlElement(name = "ErrorMessage", required = true)
  protected String errorMessage;

  /**
   * Obtient la valeur de la propriété errorCode.
   *
   * @return possible object is {@link String }
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Définit la valeur de la propriété errorCode.
   *
   * @param value allowed object is {@link String }
   */
  public void setErrorCode(String value) {
    this.errorCode = value;
  }

  /**
   * Obtient la valeur de la propriété errorMessage.
   *
   * @return possible object is {@link String }
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Définit la valeur de la propriété errorMessage.
   *
   * @param value allowed object is {@link String }
   */
  public void setErrorMessage(String value) {
    this.errorMessage = value;
  }
}
