
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for RegisteredInvoiceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisteredInvoiceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ErpDocumentNumber" type="{http://www.pangaliit.ee/arveldused/e-arve/}LongTextType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="invoiceId" use="required" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisteredInvoiceType", propOrder = {
    "erpDocumentNumber"
})
public class RegisteredInvoiceType {

    @XmlElement(name = "ErpDocumentNumber", required = true)
    protected String erpDocumentNumber;
    @XmlAttribute(name = "invoiceId", required = true)
    protected String invoiceId;

    /**
     * Gets the value of the erpDocumentNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErpDocumentNumber() {
        return erpDocumentNumber;
    }

    /**
     * Sets the value of the erpDocumentNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErpDocumentNumber(String value) {
        this.erpDocumentNumber = value;
    }

    /**
     * Gets the value of the invoiceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvoiceId() {
        return invoiceId;
    }

    /**
     * Sets the value of the invoiceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvoiceId(String value) {
        this.invoiceId = value;
    }

}
