/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Moniker" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Refinement" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="QAConfig" type="{http://www.qas.com/web-2005-02}QAConfigType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Threshold" type="{http://www.qas.com/web-2005-02}ThresholdType" />
 *       &lt;attribute name="Timeout" type="{http://www.qas.com/web-2005-02}TimeoutType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "moniker",
    "refinement",
    "qaConfig"
})
@XmlRootElement(name = "QARefine")
public class QARefine {

    @XmlElement(name = "Moniker", required = true)
    protected String moniker;
    @XmlElement(name = "Refinement", required = true)
    protected String refinement;
    @XmlElement(name = "QAConfig")
    protected QAConfigType qaConfig;
    @XmlAttribute(name = "Threshold")
    protected Integer threshold;
    @XmlAttribute(name = "Timeout")
    protected Integer timeout;

    /**
     * Obtient la valeur de la propriété moniker.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMoniker() {
        return moniker;
    }

    /**
     * Définit la valeur de la propriété moniker.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMoniker(String value) {
        this.moniker = value;
    }

    /**
     * Obtient la valeur de la propriété refinement.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefinement() {
        return refinement;
    }

    /**
     * Définit la valeur de la propriété refinement.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefinement(String value) {
        this.refinement = value;
    }

    /**
     * Obtient la valeur de la propriété qaConfig.
     * 
     * @return
     *     possible object is
     *     {@link QAConfigType }
     *     
     */
    public QAConfigType getQAConfig() {
        return qaConfig;
    }

    /**
     * Définit la valeur de la propriété qaConfig.
     * 
     * @param value
     *     allowed object is
     *     {@link QAConfigType }
     *     
     */
    public void setQAConfig(QAConfigType value) {
        this.qaConfig = value;
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
