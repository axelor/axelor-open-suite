
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for CompanyStatusRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompanyStatusRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="100" minOccurs="0"&gt;
 *         &lt;element name="RegNumber"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://www.pangaliit.ee/arveldused/e-arve/&gt;RegType"&gt;
 *                 &lt;attribute name="countryCode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/simpleContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *       &lt;attribute name="subset" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompanyStatusRequestType", propOrder = {
    "regNumber"
})
public class CompanyStatusRequestType {

    @XmlElement(name = "RegNumber")
    protected List<RegNumber> regNumber;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;
    @XmlAttribute(name = "subset")
    protected String subset;

    /**
     * Gets the value of the regNumber property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the regNumber property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegNumber().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RegNumber }
     *
     *
     */
    public List<RegNumber> getRegNumber() {
        if (regNumber == null) {
            regNumber = new ArrayList<RegNumber>();
        }
        return this.regNumber;
    }

    /**
     * Gets the value of the authPhrase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthPhrase() {
        return authPhrase;
    }

    /**
     * Sets the value of the authPhrase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthPhrase(String value) {
        this.authPhrase = value;
    }

    /**
     * Gets the value of the subset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubset() {
        return subset;
    }

    /**
     * Sets the value of the subset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubset(String value) {
        this.subset = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;simpleContent&gt;
     *     &lt;extension base="&lt;http://www.pangaliit.ee/arveldused/e-arve/&gt;RegType"&gt;
     *       &lt;attribute name="countryCode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/simpleContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class RegNumber {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "countryCode")
        protected String countryCode;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
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

}
