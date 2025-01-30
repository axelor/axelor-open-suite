
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for MailAddressENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailAddressENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PostalAddress3" type="{}LongTextType" minOccurs="0"/>
 *         &lt;element name="CountrySubDivision" type="{}LongTextType" minOccurs="0"/>
 *         &lt;element name="CountryCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailAddressENType", propOrder = {
    "postalAddress3",
    "countrySubDivision",
    "countryCode"
})
public class MailAddressENType {

    @XmlElement(name = "PostalAddress3")
    protected String postalAddress3;
    @XmlElement(name = "CountrySubDivision")
    protected String countrySubDivision;
    @XmlElement(name = "CountryCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String countryCode;

    /**
     * Gets the value of the postalAddress3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostalAddress3() {
        return postalAddress3;
    }

    /**
     * Sets the value of the postalAddress3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostalAddress3(String value) {
        this.postalAddress3 = value;
    }

    /**
     * Gets the value of the countrySubDivision property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountrySubDivision() {
        return countrySubDivision;
    }

    /**
     * Sets the value of the countrySubDivision property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountrySubDivision(String value) {
        this.countrySubDivision = value;
    }

    /**
     * Gets the value of the countryCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the value of the countryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryCode(String value) {
        this.countryCode = value;
    }

}
