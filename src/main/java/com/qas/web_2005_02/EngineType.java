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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Classe Java pour EngineType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="EngineType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.qas.com/web-2005-02>EngineEnumType">
 *       &lt;attribute name="Flatten" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="Intensity" type="{http://www.qas.com/web-2005-02}EngineIntensityType" />
 *       &lt;attribute name="PromptSet" type="{http://www.qas.com/web-2005-02}PromptSetType" />
 *       &lt;attribute name="Threshold" type="{http://www.qas.com/web-2005-02}ThresholdType" />
 *       &lt;attribute name="Timeout" type="{http://www.qas.com/web-2005-02}TimeoutType" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EngineType", propOrder = {
    "value"
})
public class EngineType {

    @XmlValue
    protected EngineEnumType value;
    @XmlAttribute(name = "Flatten")
    protected Boolean flatten;
    @XmlAttribute(name = "Intensity")
    protected EngineIntensityType intensity;
    @XmlAttribute(name = "PromptSet")
    protected PromptSetType promptSet;
    @XmlAttribute(name = "Threshold")
    protected Integer threshold;
    @XmlAttribute(name = "Timeout")
    protected Integer timeout;

    /**
     * Obtient la valeur de la propriété value.
     * 
     * @return
     *     possible object is
     *     {@link EngineEnumType }
     *     
     */
    public EngineEnumType getValue() {
        return value;
    }

    /**
     * Définit la valeur de la propriété value.
     * 
     * @param value
     *     allowed object is
     *     {@link EngineEnumType }
     *     
     */
    public void setValue(EngineEnumType value) {
        this.value = value;
    }

    /**
     * Obtient la valeur de la propriété flatten.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFlatten() {
        return flatten;
    }

    /**
     * Définit la valeur de la propriété flatten.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFlatten(Boolean value) {
        this.flatten = value;
    }

    /**
     * Obtient la valeur de la propriété intensity.
     * 
     * @return
     *     possible object is
     *     {@link EngineIntensityType }
     *     
     */
    public EngineIntensityType getIntensity() {
        return intensity;
    }

    /**
     * Définit la valeur de la propriété intensity.
     * 
     * @param value
     *     allowed object is
     *     {@link EngineIntensityType }
     *     
     */
    public void setIntensity(EngineIntensityType value) {
        this.intensity = value;
    }

    /**
     * Obtient la valeur de la propriété promptSet.
     * 
     * @return
     *     possible object is
     *     {@link PromptSetType }
     *     
     */
    public PromptSetType getPromptSet() {
        return promptSet;
    }

    /**
     * Définit la valeur de la propriété promptSet.
     * 
     * @param value
     *     allowed object is
     *     {@link PromptSetType }
     *     
     */
    public void setPromptSet(PromptSetType value) {
        this.promptSet = value;
    }

    /**
     * Obtient la valeur de la propriété threshold.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getThreshold() {
        return threshold;
    }

    /**
     * Définit la valeur de la propriété threshold.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setThreshold(Integer value) {
        this.threshold = value;
    }

    /**
     * Obtient la valeur de la propriété timeout.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Définit la valeur de la propriété timeout.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTimeout(Integer value) {
        this.timeout = value;
    }

}
