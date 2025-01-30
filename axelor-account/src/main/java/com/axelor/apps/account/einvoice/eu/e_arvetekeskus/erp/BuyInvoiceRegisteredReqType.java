
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for BuyInvoiceRegisteredReqType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BuyInvoiceRegisteredReqType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="1000" minOccurs="0"&gt;
 *         &lt;element name="RegisteredInvoice" type="{http://e-arvetekeskus.eu/erp}RegisteredInvoiceType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BuyInvoiceRegisteredReqType", propOrder = {
    "registeredInvoice"
})
public class BuyInvoiceRegisteredReqType {

    @XmlElement(name = "RegisteredInvoice")
    protected List<RegisteredInvoiceType> registeredInvoice;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;

    /**
     * Gets the value of the registeredInvoice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the registeredInvoice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegisteredInvoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RegisteredInvoiceType }
     * 
     * 
     */
    public List<RegisteredInvoiceType> getRegisteredInvoice() {
        if (registeredInvoice == null) {
            registeredInvoice = new ArrayList<RegisteredInvoiceType>();
        }
        return this.registeredInvoice;
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
