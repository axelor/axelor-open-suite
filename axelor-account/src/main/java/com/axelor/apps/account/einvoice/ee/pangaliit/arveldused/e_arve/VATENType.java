
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for VATENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VATENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CategoryCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *         &lt;element name="ExemptionReasonCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ExemptionReasonText" type="{}LongTextType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VATENType", propOrder = {
    "categoryCode",
    "exemptionReasonCode",
    "exemptionReasonText"
})
public class VATENType {

    @XmlElement(name = "CategoryCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String categoryCode;
    @XmlElement(name = "ExemptionReasonCode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String exemptionReasonCode;
    @XmlElement(name = "ExemptionReasonText")
    protected String exemptionReasonText;

    /**
     * Gets the value of the categoryCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategoryCode() {
        return categoryCode;
    }

    /**
     * Sets the value of the categoryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategoryCode(String value) {
        this.categoryCode = value;
    }

    /**
     * Gets the value of the exemptionReasonCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExemptionReasonCode() {
        return exemptionReasonCode;
    }

    /**
     * Sets the value of the exemptionReasonCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExemptionReasonCode(String value) {
        this.exemptionReasonCode = value;
    }

    /**
     * Gets the value of the exemptionReasonText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExemptionReasonText() {
        return exemptionReasonText;
    }

    /**
     * Sets the value of the exemptionReasonText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExemptionReasonText(String value) {
        this.exemptionReasonText = value;
    }

}
