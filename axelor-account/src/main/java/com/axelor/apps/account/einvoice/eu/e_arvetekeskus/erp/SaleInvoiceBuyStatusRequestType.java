
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for SaleInvoiceBuyStatusRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SaleInvoiceBuyStatusRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="buyEvent" type="{http://e-arvetekeskus.eu/erp}BuyInvoiceEventCodeType" maxOccurs="10"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="latestChangeId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SaleInvoiceBuyStatusRequestType", propOrder = {
    "buyEvent"
})
public class SaleInvoiceBuyStatusRequestType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected List<BuyInvoiceEventCodeType> buyEvent;
    @XmlAttribute(name = "latestChangeId", required = true)
    protected long latestChangeId;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;

    /**
     * Gets the value of the buyEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the buyEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBuyEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BuyInvoiceEventCodeType }
     * 
     * 
     */
    public List<BuyInvoiceEventCodeType> getBuyEvent() {
        if (buyEvent == null) {
            buyEvent = new ArrayList<BuyInvoiceEventCodeType>();
        }
        return this.buyEvent;
    }

    /**
     * Gets the value of the latestChangeId property.
     * 
     */
    public long getLatestChangeId() {
        return latestChangeId;
    }

    /**
     * Sets the value of the latestChangeId property.
     * 
     */
    public void setLatestChangeId(long value) {
        this.latestChangeId = value;
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
