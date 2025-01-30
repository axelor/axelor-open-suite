
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for RejectConfirmationRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RejectConfirmationRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="RejectContent" type="{http://e-arvetekeskus.eu/erp}RejectContentType" maxOccurs="5"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType" /&gt;
 *       &lt;attribute name="regNumber" use="required" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType" /&gt;
 *       &lt;attribute name="countryCode" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType" /&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RejectConfirmationRequestType", propOrder = {
    "rejectContent"
})
public class RejectConfirmationRequestType {

    @XmlElement(name = "RejectContent", required = true)
    protected List<RejectContentType> rejectContent;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "regNumber", required = true)
    protected String regNumber;
    @XmlAttribute(name = "countryCode")
    protected String countryCode;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;

    /**
     * Gets the value of the rejectContent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rejectContent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRejectContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RejectContentType }
     * 
     * 
     */
    public List<RejectContentType> getRejectContent() {
        if (rejectContent == null) {
            rejectContent = new ArrayList<RejectContentType>();
        }
        return this.rejectContent;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the regNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegNumber() {
        return regNumber;
    }

    /**
     * Sets the value of the regNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegNumber(String value) {
        this.regNumber = value;
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

}
