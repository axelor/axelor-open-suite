
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;


/**
 * <p>Java class for VATRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VATRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SumBeforeVAT" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="VATRate" type="{}Decimal2FractionDigitsType"/>
 *         &lt;element name="VATSum" type="{}Decimal4FractionDigitsType"/>
 *         &lt;element name="Currency" type="{}CurrencyType" minOccurs="0"/>
 *         &lt;element name="SumAfterVAT" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="Reference" type="{}ExtensionRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="vatId" type="{}VatCodeType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VATRecord", propOrder = {
    "sumBeforeVAT",
    "vatRate",
    "vatSum",
    "currency",
    "sumAfterVAT",
    "reference"
})
public class VATRecord {

    @XmlElement(name = "SumBeforeVAT")
    protected BigDecimal sumBeforeVAT;
    @XmlElement(name = "VATRate", required = true)
    protected BigDecimal vatRate;
    @XmlElement(name = "VATSum", required = true)
    protected BigDecimal vatSum;
    @XmlElement(name = "Currency")
    protected String currency;
    @XmlElement(name = "SumAfterVAT")
    protected BigDecimal sumAfterVAT;
    @XmlElement(name = "Reference")
    protected ExtensionRecord reference;
    @XmlAttribute(name = "vatId")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String vatId;

    /**
     * Gets the value of the sumBeforeVAT property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSumBeforeVAT() {
        return sumBeforeVAT;
    }

    /**
     * Sets the value of the sumBeforeVAT property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSumBeforeVAT(BigDecimal value) {
        this.sumBeforeVAT = value;
    }

    /**
     * Gets the value of the vatRate property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getVATRate() {
        return vatRate;
    }

    /**
     * Sets the value of the vatRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setVATRate(BigDecimal value) {
        this.vatRate = value;
    }

    /**
     * Gets the value of the vatSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getVATSum() {
        return vatSum;
    }

    /**
     * Sets the value of the vatSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setVATSum(BigDecimal value) {
        this.vatSum = value;
    }

    /**
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * Gets the value of the sumAfterVAT property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSumAfterVAT() {
        return sumAfterVAT;
    }

    /**
     * Sets the value of the sumAfterVAT property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSumAfterVAT(BigDecimal value) {
        this.sumAfterVAT = value;
    }

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionRecord }
     *     
     */
    public ExtensionRecord getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionRecord }
     *     
     */
    public void setReference(ExtensionRecord value) {
        this.reference = value;
    }

    /**
     * Gets the value of the vatId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVatId() {
        return vatId;
    }

    /**
     * Sets the value of the vatId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVatId(String value) {
        this.vatId = value;
    }

}
