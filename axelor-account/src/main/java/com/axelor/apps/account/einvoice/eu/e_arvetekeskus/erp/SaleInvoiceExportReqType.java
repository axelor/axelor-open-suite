
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for SaleInvoiceExportReqType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SaleInvoiceExportReqType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element name="state" type="{http://e-arvetekeskus.eu/erp}SaleInvoiceStateType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="since" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *       &lt;attribute name="format" type="{http://e-arvetekeskus.eu/erp}InvoiceFormatType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SaleInvoiceExportReqType", propOrder = {
    "state"
})
public class SaleInvoiceExportReqType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected List<SaleInvoiceStateType> state;
    @XmlAttribute(name = "since", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar since;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;
    @XmlAttribute(name = "format")
    protected InvoiceFormatType format;

    /**
     * Gets the value of the state property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the state property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getState().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SaleInvoiceStateType }
     * 
     * 
     */
    public List<SaleInvoiceStateType> getState() {
        if (state == null) {
            state = new ArrayList<SaleInvoiceStateType>();
        }
        return this.state;
    }

    /**
     * Gets the value of the since property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSince() {
        return since;
    }

    /**
     * Sets the value of the since property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSince(XMLGregorianCalendar value) {
        this.since = value;
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
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceFormatType }
     *     
     */
    public InvoiceFormatType getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceFormatType }
     *     
     */
    public void setFormat(InvoiceFormatType value) {
        this.format = value;
    }

}
