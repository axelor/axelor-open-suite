/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */

package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour QAConfigType complex type.
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
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QAConfigType", propOrder = {
    "iniFile",
    "iniSection"
})
public class QAConfigType {

    @XmlElement(name = "IniFile")
    protected String iniFile;
    @XmlElement(name = "IniSection")
    protected String iniSection;

    /**
     * Obtient la valeur de la propriété iniFile.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIniFile() {
        return iniFile;
    }

    /**
     * Définit la valeur de la propriété iniFile.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIniFile(String value) {
        this.iniFile = value;
    }

    /**
     * Obtient la valeur de la propriété iniSection.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIniSection() {
        return iniSection;
    }

    /**
     * Définit la valeur de la propriété iniSection.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIniSection(String value) {
        this.iniSection = value;
    }

}
