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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour PromptLine complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="PromptLine">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Prompt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SuggestedInputLength" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="Example" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "PromptLine",
    propOrder = {"prompt", "suggestedInputLength", "example"})
public class PromptLine {

  @XmlElement(name = "Prompt", required = true)
  protected String prompt;

  @XmlElement(name = "SuggestedInputLength", required = true)
  @XmlSchemaType(name = "nonNegativeInteger")
  protected BigInteger suggestedInputLength;

  @XmlElement(name = "Example", required = true)
  protected String example;

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
   * Obtient la valeur de la propriété suggestedInputLength.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getSuggestedInputLength() {
    return suggestedInputLength;
  }

  /**
   * Définit la valeur de la propriété suggestedInputLength.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setSuggestedInputLength(BigInteger value) {
    this.suggestedInputLength = value;
  }

  /**
   * Obtient la valeur de la propriété example.
   *
   * @return possible object is {@link String }
   */
  public String getExample() {
    return example;
  }

  /**
   * Définit la valeur de la propriété example.
   *
   * @param value allowed object is {@link String }
   */
  public void setExample(String value) {
    this.example = value;
  }
}
