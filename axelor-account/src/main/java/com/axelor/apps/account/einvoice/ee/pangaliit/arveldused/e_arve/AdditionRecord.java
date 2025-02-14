
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;


/**
 * <p>Java class for AdditionRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AdditionRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AddContent" type="{}NormalTextType"/>
 *         &lt;element name="AddRate" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="AddSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="addCode" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;pattern value="DSC"/>
 *             &lt;pattern value="CHR"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdditionRecord", propOrder = {
    "addContent",
    "addRate",
    "addSum"
})
public class AdditionRecord {

    @XmlElement(name = "AddContent", required = true)
    protected String addContent;
    @XmlElement(name = "AddRate")
    protected BigDecimal addRate;
    @XmlElement(name = "AddSum")
    protected BigDecimal addSum;
    @XmlAttribute(name = "addCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String addCode;

    /**
     * Gets the value of the addContent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddContent() {
        return addContent;
    }

    /**
     * Sets the value of the addContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddContent(String value) {
        this.addContent = value;
    }

    /**
     * Gets the value of the addRate property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAddRate() {
        return addRate;
    }

    /**
     * Sets the value of the addRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAddRate(BigDecimal value) {
        this.addRate = value;
    }

    /**
     * Gets the value of the addSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAddSum() {
        return addSum;
    }

    /**
     * Sets the value of the addSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAddSum(BigDecimal value) {
        this.addSum = value;
    }

    /**
     * Gets the value of the addCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddCode() {
        return addCode;
    }

    /**
     * Sets the value of the addCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddCode(String value) {
        this.addCode = value;
    }

}
