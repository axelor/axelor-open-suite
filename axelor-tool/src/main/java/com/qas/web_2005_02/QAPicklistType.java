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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour QAPicklistType complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="QAPicklistType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FullPicklistMoniker" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PicklistEntry" type="{http://www.qas.com/web-2005-02}PicklistEntryType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Prompt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Total" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *       &lt;/sequence>
 *       &lt;attribute name="AutoFormatSafe" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="AutoFormatPastClose" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="AutoStepinSafe" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="AutoStepinPastClose" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="LargePotential" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="MaxMatches" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="MoreOtherMatches" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="OverThreshold" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Timeout" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "QAPicklistType",
    propOrder = {"fullPicklistMoniker", "picklistEntry", "prompt", "total"})
public class QAPicklistType {

  @XmlElement(name = "FullPicklistMoniker", required = true)
  protected String fullPicklistMoniker;

  @XmlElement(name = "PicklistEntry")
  protected List<PicklistEntryType> picklistEntry;

  @XmlElement(name = "Prompt", required = true)
  protected String prompt;

  @XmlElement(name = "Total", required = true)
  @XmlSchemaType(name = "nonNegativeInteger")
  protected BigInteger total;

  @XmlAttribute(name = "AutoFormatSafe")
  protected Boolean autoFormatSafe;

  @XmlAttribute(name = "AutoFormatPastClose")
  protected Boolean autoFormatPastClose;

  @XmlAttribute(name = "AutoStepinSafe")
  protected Boolean autoStepinSafe;

  @XmlAttribute(name = "AutoStepinPastClose")
  protected Boolean autoStepinPastClose;

  @XmlAttribute(name = "LargePotential")
  protected Boolean largePotential;

  @XmlAttribute(name = "MaxMatches")
  protected Boolean maxMatches;

  @XmlAttribute(name = "MoreOtherMatches")
  protected Boolean moreOtherMatches;

  @XmlAttribute(name = "OverThreshold")
  protected Boolean overThreshold;

  @XmlAttribute(name = "Timeout")
  protected Boolean timeout;

  /**
   * Obtient la valeur de la propriété fullPicklistMoniker.
   *
   * @return possible object is {@link String }
   */
  public String getFullPicklistMoniker() {
    return fullPicklistMoniker;
  }

  /**
   * Définit la valeur de la propriété fullPicklistMoniker.
   *
   * @param value allowed object is {@link String }
   */
  public void setFullPicklistMoniker(String value) {
    this.fullPicklistMoniker = value;
  }

  /**
   * Gets the value of the picklistEntry property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the picklistEntry property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getPicklistEntry().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link PicklistEntryType }
   */
  public List<PicklistEntryType> getPicklistEntry() {
    if (picklistEntry == null) {
      picklistEntry = new ArrayList<PicklistEntryType>();
    }
    return this.picklistEntry;
  }

  /**
   * Obtient la valeur de la propriété prompt.
   *
   * @return possible object is {@link String }
   */
  public String getPrompt() {
    return prompt;
  }

  /**
   * Définit la valeur de la propriété prompt.
   *
   * @param value allowed object is {@link String }
   */
  public void setPrompt(String value) {
    this.prompt = value;
  }

  /**
   * Obtient la valeur de la propriété total.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getTotal() {
    return total;
  }

  /**
   * Définit la valeur de la propriété total.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setTotal(BigInteger value) {
    this.total = value;
  }

  /**
   * Obtient la valeur de la propriété autoFormatSafe.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isAutoFormatSafe() {
    if (autoFormatSafe == null) {
      return false;
    } else {
      return autoFormatSafe;
    }
  }

  /**
   * Définit la valeur de la propriété autoFormatSafe.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setAutoFormatSafe(Boolean value) {
    this.autoFormatSafe = value;
  }

  /**
   * Obtient la valeur de la propriété autoFormatPastClose.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isAutoFormatPastClose() {
    if (autoFormatPastClose == null) {
      return false;
    } else {
      return autoFormatPastClose;
    }
  }

  /**
   * Définit la valeur de la propriété autoFormatPastClose.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setAutoFormatPastClose(Boolean value) {
    this.autoFormatPastClose = value;
  }

  /**
   * Obtient la valeur de la propriété autoStepinSafe.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isAutoStepinSafe() {
    if (autoStepinSafe == null) {
      return false;
    } else {
      return autoStepinSafe;
    }
  }

  /**
   * Définit la valeur de la propriété autoStepinSafe.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setAutoStepinSafe(Boolean value) {
    this.autoStepinSafe = value;
  }

  /**
   * Obtient la valeur de la propriété autoStepinPastClose.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isAutoStepinPastClose() {
    if (autoStepinPastClose == null) {
      return false;
    } else {
      return autoStepinPastClose;
    }
  }

  /**
   * Définit la valeur de la propriété autoStepinPastClose.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setAutoStepinPastClose(Boolean value) {
    this.autoStepinPastClose = value;
  }

  /**
   * Obtient la valeur de la propriété largePotential.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isLargePotential() {
    if (largePotential == null) {
      return false;
    } else {
      return largePotential;
    }
  }

  /**
   * Définit la valeur de la propriété largePotential.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setLargePotential(Boolean value) {
    this.largePotential = value;
  }

  /**
   * Obtient la valeur de la propriété maxMatches.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isMaxMatches() {
    if (maxMatches == null) {
      return false;
    } else {
      return maxMatches;
    }
  }

  /**
   * Définit la valeur de la propriété maxMatches.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setMaxMatches(Boolean value) {
    this.maxMatches = value;
  }

  /**
   * Obtient la valeur de la propriété moreOtherMatches.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isMoreOtherMatches() {
    if (moreOtherMatches == null) {
      return false;
    } else {
      return moreOtherMatches;
    }
  }

  /**
   * Définit la valeur de la propriété moreOtherMatches.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setMoreOtherMatches(Boolean value) {
    this.moreOtherMatches = value;
  }

  /**
   * Obtient la valeur de la propriété overThreshold.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isOverThreshold() {
    if (overThreshold == null) {
      return false;
    } else {
      return overThreshold;
    }
  }

  /**
   * Définit la valeur de la propriété overThreshold.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setOverThreshold(Boolean value) {
    this.overThreshold = value;
  }

  /**
   * Obtient la valeur de la propriété timeout.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isTimeout() {
    if (timeout == null) {
      return false;
    } else {
      return timeout;
    }
  }

  /**
   * Définit la valeur de la propriété timeout.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setTimeout(Boolean value) {
    this.timeout = value;
  }
}
