
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for SaleInvoiceBuyStatusResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SaleInvoiceBuyStatusResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="response" type="{http://e-arvetekeskus.eu/erp}SaleInvoiceBuyStatusType" maxOccurs="1000" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="latestChangeId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="includesLatest" use="required" type="{http://e-arvetekeskus.eu/erp}YesNoType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SaleInvoiceBuyStatusResponseType", propOrder = {
    "response"
})
public class SaleInvoiceBuyStatusResponseType {

    protected List<SaleInvoiceBuyStatusType> response;
    @XmlAttribute(name = "latestChangeId", required = true)
    protected long latestChangeId;
    @XmlAttribute(name = "includesLatest", required = true)
    protected YesNoType includesLatest;

    /**
     * Gets the value of the response property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the response property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResponse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SaleInvoiceBuyStatusType }
     * 
     * 
     */
    public List<SaleInvoiceBuyStatusType> getResponse() {
        if (response == null) {
            response = new ArrayList<SaleInvoiceBuyStatusType>();
        }
        return this.response;
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
     * Gets the value of the includesLatest property.
     * 
     * @return
     *     possible object is
     *     {@link YesNoType }
     *     
     */
    public YesNoType getIncludesLatest() {
        return includesLatest;
    }

    /**
     * Sets the value of the includesLatest property.
     * 
     * @param value
     *     allowed object is
     *     {@link YesNoType }
     *     
     */
    public void setIncludesLatest(YesNoType value) {
        this.includesLatest = value;
    }

}
