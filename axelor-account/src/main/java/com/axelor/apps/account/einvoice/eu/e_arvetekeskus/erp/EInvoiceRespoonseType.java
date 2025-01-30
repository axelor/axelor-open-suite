
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve.EInvoice;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * DEPRECATED: BuyInvoiceExportRequest and EInvoiceResponseType should be used instead
 * 
 * <p>Java class for EInvoiceRespoonseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EInvoiceRespoonseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.pangaliit.ee/arveldused/e-arve/}E_Invoice"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="includesLatest" use="required" type="{http://www.pangaliit.ee/arveldused/e-arve/}YesNoType" /&gt;
 *       &lt;attribute name="latestChange" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EInvoiceRespoonseType", propOrder = {
    "eInvoice"
})
public class EInvoiceRespoonseType {

    @XmlElement(name = "E_Invoice", namespace = "http://www.pangaliit.ee/arveldused/e-arve/", required = true)
    protected EInvoice eInvoice;
    @XmlAttribute(name = "includesLatest", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String includesLatest;
    @XmlAttribute(name = "latestChange", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar latestChange;

    /**
     * Gets the value of the eInvoice property.
     * 
     * @return
     *     possible object is
     *     {@link EInvoice }
     *     
     */
    public EInvoice getEInvoice() {
        return eInvoice;
    }

    /**
     * Sets the value of the eInvoice property.
     * 
     * @param value
     *     allowed object is
     *     {@link EInvoice }
     *     
     */
    public void setEInvoice(EInvoice value) {
        this.eInvoice = value;
    }

    /**
     * Gets the value of the includesLatest property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIncludesLatest() {
        return includesLatest;
    }

    /**
     * Sets the value of the includesLatest property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludesLatest(String value) {
        this.includesLatest = value;
    }

    /**
     * Gets the value of the latestChange property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLatestChange() {
        return latestChange;
    }

    /**
     * Sets the value of the latestChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLatestChange(XMLGregorianCalendar value) {
        this.latestChange = value;
    }

}
