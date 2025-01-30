
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for InvoiceAttachmentResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvoiceAttachmentResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="20" minOccurs="0"&gt;
 *         &lt;element name="InvoiceAttachment" type="{http://e-arvetekeskus.eu/erp}InvoiceAttachmentType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="nextAttachmentIndex" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvoiceAttachmentResponseType", propOrder = {
    "invoiceAttachment"
})
public class InvoiceAttachmentResponseType {

    @XmlElement(name = "InvoiceAttachment")
    protected List<InvoiceAttachmentType> invoiceAttachment;
    @XmlAttribute(name = "nextAttachmentIndex", required = true)
    protected BigInteger nextAttachmentIndex;

    /**
     * Gets the value of the invoiceAttachment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the invoiceAttachment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvoiceAttachment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InvoiceAttachmentType }
     * 
     * 
     */
    public List<InvoiceAttachmentType> getInvoiceAttachment() {
        if (invoiceAttachment == null) {
            invoiceAttachment = new ArrayList<InvoiceAttachmentType>();
        }
        return this.invoiceAttachment;
    }

    /**
     * Gets the value of the nextAttachmentIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNextAttachmentIndex() {
        return nextAttachmentIndex;
    }

    /**
     * Sets the value of the nextAttachmentIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNextAttachmentIndex(BigInteger value) {
        this.nextAttachmentIndex = value;
    }

}
