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

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour PicklistEntryType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="PicklistEntryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Moniker" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PartialAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Picklist" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Postcode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Score" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *       &lt;/sequence>
 *       &lt;attribute name="FullAddress" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Multiples" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="CanStep" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="AliasMatch" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="PostcodeRecoded" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="CrossBorderMatch" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="DummyPOBox" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Information" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="WarnInformation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="IncompleteAddr" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="UnresolvableRange" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="PhantomPrimaryPoint" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PicklistEntryType", propOrder = {
    "moniker",
    "partialAddress",
    "picklist",
    "postcode",
    "score"
})
public class PicklistEntryType {

    @XmlElement(name = "Moniker", required = true)
    protected String moniker;
    @XmlElement(name = "PartialAddress", required = true)
    protected String partialAddress;
    @XmlElement(name = "Picklist", required = true)
    protected String picklist;
    @XmlElement(name = "Postcode", required = true)
    protected String postcode;
    @XmlElement(name = "Score", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger score;
    @XmlAttribute(name = "FullAddress")
    protected Boolean fullAddress;
    @XmlAttribute(name = "Multiples")
    protected Boolean multiples;
    @XmlAttribute(name = "CanStep")
    protected Boolean canStep;
    @XmlAttribute(name = "AliasMatch")
    protected Boolean aliasMatch;
    @XmlAttribute(name = "PostcodeRecoded")
    protected Boolean postcodeRecoded;
    @XmlAttribute(name = "CrossBorderMatch")
    protected Boolean crossBorderMatch;
    @XmlAttribute(name = "DummyPOBox")
    protected Boolean dummyPOBox;
    @XmlAttribute(name = "Name")
    protected Boolean name;
    @XmlAttribute(name = "Information")
    protected Boolean information;
    @XmlAttribute(name = "WarnInformation")
    protected Boolean warnInformation;
    @XmlAttribute(name = "IncompleteAddr")
    protected Boolean incompleteAddr;
    @XmlAttribute(name = "UnresolvableRange")
    protected Boolean unresolvableRange;
    @XmlAttribute(name = "PhantomPrimaryPoint")
    protected Boolean phantomPrimaryPoint;

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
     * Obtient la valeur de la propriété partialAddress.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartialAddress() {
        return partialAddress;
    }

    /**
     * Définit la valeur de la propriété partialAddress.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartialAddress(String value) {
        this.partialAddress = value;
    }

    /**
     * Obtient la valeur de la propriété picklist.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPicklist() {
        return picklist;
    }

    /**
     * Définit la valeur de la propriété picklist.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPicklist(String value) {
        this.picklist = value;
    }

    /**
     * Obtient la valeur de la propriété postcode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Définit la valeur de la propriété postcode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostcode(String value) {
        this.postcode = value;
    }

    /**
     * Obtient la valeur de la propriété score.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getScore() {
        return score;
    }

    /**
     * Définit la valeur de la propriété score.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setScore(BigInteger value) {
        this.score = value;
    }

    /**
     * Obtient la valeur de la propriété fullAddress.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFullAddress() {
        if (fullAddress == null) {
            return false;
        } else {
            return fullAddress;
        }
    }

    /**
     * Définit la valeur de la propriété fullAddress.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFullAddress(Boolean value) {
        this.fullAddress = value;
    }

    /**
     * Obtient la valeur de la propriété multiples.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMultiples() {
        if (multiples == null) {
            return false;
        } else {
            return multiples;
        }
    }

    /**
     * Définit la valeur de la propriété multiples.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMultiples(Boolean value) {
        this.multiples = value;
    }

    /**
     * Obtient la valeur de la propriété canStep.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCanStep() {
        if (canStep == null) {
            return false;
        } else {
            return canStep;
        }
    }

    /**
     * Définit la valeur de la propriété canStep.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanStep(Boolean value) {
        this.canStep = value;
    }

    /**
     * Obtient la valeur de la propriété aliasMatch.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAliasMatch() {
        if (aliasMatch == null) {
            return false;
        } else {
            return aliasMatch;
        }
    }

    /**
     * Définit la valeur de la propriété aliasMatch.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAliasMatch(Boolean value) {
        this.aliasMatch = value;
    }

    /**
     * Obtient la valeur de la propriété postcodeRecoded.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPostcodeRecoded() {
        if (postcodeRecoded == null) {
            return false;
        } else {
            return postcodeRecoded;
        }
    }

    /**
     * Définit la valeur de la propriété postcodeRecoded.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPostcodeRecoded(Boolean value) {
        this.postcodeRecoded = value;
    }

    /**
     * Obtient la valeur de la propriété crossBorderMatch.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCrossBorderMatch() {
        if (crossBorderMatch == null) {
            return false;
        } else {
            return crossBorderMatch;
        }
    }

    /**
     * Définit la valeur de la propriété crossBorderMatch.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCrossBorderMatch(Boolean value) {
        this.crossBorderMatch = value;
    }

    /**
     * Obtient la valeur de la propriété dummyPOBox.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDummyPOBox() {
        if (dummyPOBox == null) {
            return false;
        } else {
            return dummyPOBox;
        }
    }

    /**
     * Définit la valeur de la propriété dummyPOBox.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDummyPOBox(Boolean value) {
        this.dummyPOBox = value;
    }

    /**
     * Obtient la valeur de la propriété name.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isName() {
        if (name == null) {
            return false;
        } else {
            return name;
        }
    }

    /**
     * Définit la valeur de la propriété name.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setName(Boolean value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété information.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInformation() {
        if (information == null) {
            return false;
        } else {
            return information;
        }
    }

    /**
     * Définit la valeur de la propriété information.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInformation(Boolean value) {
        this.information = value;
    }

    /**
     * Obtient la valeur de la propriété warnInformation.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isWarnInformation() {
        if (warnInformation == null) {
            return false;
        } else {
            return warnInformation;
        }
    }

    /**
     * Définit la valeur de la propriété warnInformation.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWarnInformation(Boolean value) {
        this.warnInformation = value;
    }

    /**
     * Obtient la valeur de la propriété incompleteAddr.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncompleteAddr() {
        if (incompleteAddr == null) {
            return false;
        } else {
            return incompleteAddr;
        }
    }

    /**
     * Définit la valeur de la propriété incompleteAddr.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncompleteAddr(Boolean value) {
        this.incompleteAddr = value;
    }

    /**
     * Obtient la valeur de la propriété unresolvableRange.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUnresolvableRange() {
        if (unresolvableRange == null) {
            return false;
        } else {
            return unresolvableRange;
        }
    }

    /**
     * Définit la valeur de la propriété unresolvableRange.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnresolvableRange(Boolean value) {
        this.unresolvableRange = value;
    }

    /**
     * Obtient la valeur de la propriété phantomPrimaryPoint.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPhantomPrimaryPoint() {
        if (phantomPrimaryPoint == null) {
            return false;
        } else {
            return phantomPrimaryPoint;
        }
    }

    /**
     * Définit la valeur de la propriété phantomPrimaryPoint.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPhantomPrimaryPoint(Boolean value) {
        this.phantomPrimaryPoint = value;
    }

}
