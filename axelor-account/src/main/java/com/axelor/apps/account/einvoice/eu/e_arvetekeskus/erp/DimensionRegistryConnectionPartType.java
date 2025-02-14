
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DimensionRegistryConnectionPartType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DimensionRegistryConnectionPartType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Dimension" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType"/&gt;
 *         &lt;element name="DimensionNum" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DimensionRegistryConnectionPartType", propOrder = {
    "dimension",
    "dimensionNum"
})
public class DimensionRegistryConnectionPartType {

    @XmlElement(name = "Dimension", required = true)
    protected String dimension;
    @XmlElement(name = "DimensionNum", required = true)
    protected String dimensionNum;

    /**
     * Gets the value of the dimension property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDimension() {
        return dimension;
    }

    /**
     * Sets the value of the dimension property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDimension(String value) {
        this.dimension = value;
    }

    /**
     * Gets the value of the dimensionNum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDimensionNum() {
        return dimensionNum;
    }

    /**
     * Sets the value of the dimensionNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDimensionNum(String value) {
        this.dimensionNum = value;
    }

}
