
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for SaleInvoiceBuyStatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SaleInvoiceBuyStatusType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="infoCode" type="{http://e-arvetekeskus.eu/erp}ShortTextType" minOccurs="0"/&gt;
 *         &lt;element name="infoText" type="{http://e-arvetekeskus.eu/erp}LongTextType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="changeId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="changeDateTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="invoiceNr" use="required" type="{http://e-arvetekeskus.eu/erp}NormalTextType" /&gt;
 *       &lt;attribute name="invoiceDate" use="required" type="{http://www.w3.org/2001/XMLSchema}date" /&gt;
 *       &lt;attribute name="event" use="required" type="{http://e-arvetekeskus.eu/erp}BuyInvoiceEventCodeType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SaleInvoiceBuyStatusType", propOrder = {
    "infoCode",
    "infoText"
})
public class SaleInvoiceBuyStatusType {

    protected String infoCode;
    protected String infoText;
    @XmlAttribute(name = "changeId", required = true)
    protected long changeId;
    @XmlAttribute(name = "changeDateTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar changeDateTime;
    @XmlAttribute(name = "invoiceNr", required = true)
    protected String invoiceNr;
    @XmlAttribute(name = "invoiceDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar invoiceDate;
    @XmlAttribute(name = "event", required = true)
    protected BuyInvoiceEventCodeType event;

    /**
     * Gets the value of the infoCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoCode() {
        return infoCode;
    }

    /**
     * Sets the value of the infoCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoCode(String value) {
        this.infoCode = value;
    }

    /**
     * Gets the value of the infoText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoText() {
        return infoText;
    }

    /**
     * Sets the value of the infoText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoText(String value) {
        this.infoText = value;
    }

    /**
     * Gets the value of the changeId property.
     * 
     */
    public long getChangeId() {
        return changeId;
    }

    /**
     * Sets the value of the changeId property.
     * 
     */
    public void setChangeId(long value) {
        this.changeId = value;
    }

    /**
     * Gets the value of the changeDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getChangeDateTime() {
        return changeDateTime;
    }

    /**
     * Sets the value of the changeDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setChangeDateTime(XMLGregorianCalendar value) {
        this.changeDateTime = value;
    }

    /**
     * Gets the value of the invoiceNr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvoiceNr() {
        return invoiceNr;
    }

    /**
     * Sets the value of the invoiceNr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvoiceNr(String value) {
        this.invoiceNr = value;
    }

    /**
     * Gets the value of the invoiceDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getInvoiceDate() {
        return invoiceDate;
    }

    /**
     * Sets the value of the invoiceDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setInvoiceDate(XMLGregorianCalendar value) {
        this.invoiceDate = value;
    }

    /**
     * Gets the value of the event property.
     * 
     * @return
     *     possible object is
     *     {@link BuyInvoiceEventCodeType }
     *     
     */
    public BuyInvoiceEventCodeType getEvent() {
        return event;
    }

    /**
     * Sets the value of the event property.
     * 
     * @param value
     *     allowed object is
     *     {@link BuyInvoiceEventCodeType }
     *     
     */
    public void setEvent(BuyInvoiceEventCodeType value) {
        this.event = value;
    }

}
