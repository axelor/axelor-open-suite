/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
 *         &lt;element name="Line" type="{http://www.qas.com/web-2005-02}PromptLine" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Dynamic" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"line"})
@XmlRootElement(name = "QAPromptSet")
public class QAPromptSet {

  @XmlElement(name = "Line")
  protected List<PromptLine> line;

  @XmlAttribute(name = "Dynamic")
  protected Boolean dynamic;

  /**
   * Gets the value of the line property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the line property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getLine().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link PromptLine }
   */
  public List<PromptLine> getLine() {
    if (line == null) {
      line = new ArrayList<PromptLine>();
    }
    return this.line;
  }

  /**
   * Obtient la valeur de la propriété dynamic.
   *
   * @return possible object is {@link Boolean }
   */
  public boolean isDynamic() {
    if (dynamic == null) {
      return false;
    } else {
      return dynamic;
    }
  }

  /**
   * Définit la valeur de la propriété dynamic.
   *
   * @param value allowed object is {@link Boolean }
   */
  public void setDynamic(Boolean value) {
    this.dynamic = value;
  }
}
