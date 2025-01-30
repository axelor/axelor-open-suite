
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;


/**
 * <p>Java class for AdditionENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AdditionENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AddContent" type="{}LongTextType" minOccurs="0"/>
 *         &lt;element name="AddContentCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="AddBaseSum" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="AddRate" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="AddSum" type="{}Decimal2FractionDigitsType"/>
 *         &lt;element name="VAT" type="{}VATRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="addCode" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdditionENType", propOrder = {
    "addContent",
    "addContentCode",
    "addBaseSum",
    "addRate",
    "addSum",
    "vat"
})
public class AdditionENType {

    @XmlElement(name = "AddContent")
    protected String addContent;
    @XmlElement(name = "AddContentCode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String addContentCode;
    @XmlElement(name = "AddBaseSum")
    protected BigDecimal addBaseSum;
    @XmlElement(name = "AddRate")
    protected BigDecimal addRate;
    @XmlElement(name = "AddSum", required = true)
    protected BigDecimal addSum;
    @XmlElement(name = "VAT")
    protected VATRecord vat;
    @XmlAttribute(name = "addCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
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
     * Gets the value of the addContentCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddContentCode() {
        return addContentCode;
    }

    /**
     * Sets the value of the addContentCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddContentCode(String value) {
        this.addContentCode = value;
    }

    /**
     * Gets the value of the addBaseSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAddBaseSum() {
        return addBaseSum;
    }

    /**
     * Sets the value of the addBaseSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAddBaseSum(BigDecimal value) {
        this.addBaseSum = value;
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
     * Gets the value of the vat property.
     * 
     * @return
     *     possible object is
     *     {@link VATRecord }
     *     
     */
    public VATRecord getVAT() {
        return vat;
    }

    /**
     * Sets the value of the vat property.
     * 
     * @param value
     *     allowed object is
     *     {@link VATRecord }
     *     
     */
    public void setVAT(VATRecord value) {
        this.vat = value;
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
