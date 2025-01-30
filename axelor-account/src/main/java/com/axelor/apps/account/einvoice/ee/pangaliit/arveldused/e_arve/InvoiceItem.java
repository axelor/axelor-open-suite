
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}InvoiceTotalGroup" minOccurs="0"/>
 *         &lt;element ref="{}InvoiceItemGroup" maxOccurs="unbounded"/>
 *         &lt;element ref="{}InvoiceItemTotalGroup" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "invoiceTotalGroup",
    "invoiceItemGroup",
    "invoiceItemTotalGroup"
})
@XmlRootElement(name = "InvoiceItem")
public class InvoiceItem {

    @XmlElement(name = "InvoiceTotalGroup")
    protected InvoiceTotalGroup invoiceTotalGroup;
    @XmlElement(name = "InvoiceItemGroup", required = true)
    protected List<InvoiceItemGroup> invoiceItemGroup;
    @XmlElement(name = "InvoiceItemTotalGroup")
    protected InvoiceItemTotalGroup invoiceItemTotalGroup;

    /**
     * Gets the value of the invoiceTotalGroup property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceTotalGroup }
     *     
     */
    public InvoiceTotalGroup getInvoiceTotalGroup() {
        return invoiceTotalGroup;
    }

    /**
     * Sets the value of the invoiceTotalGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceTotalGroup }
     *     
     */
    public void setInvoiceTotalGroup(InvoiceTotalGroup value) {
        this.invoiceTotalGroup = value;
    }

    /**
     * Gets the value of the invoiceItemGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the invoiceItemGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvoiceItemGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InvoiceItemGroup }
     * 
     * 
     */
    public List<InvoiceItemGroup> getInvoiceItemGroup() {
        if (invoiceItemGroup == null) {
            invoiceItemGroup = new ArrayList<InvoiceItemGroup>();
        }
        return this.invoiceItemGroup;
    }

    /**
     * Gets the value of the invoiceItemTotalGroup property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceItemTotalGroup }
     *     
     */
    public InvoiceItemTotalGroup getInvoiceItemTotalGroup() {
        return invoiceItemTotalGroup;
    }

    /**
     * Sets the value of the invoiceItemTotalGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceItemTotalGroup }
     *     
     */
    public void setInvoiceItemTotalGroup(InvoiceItemTotalGroup value) {
        this.invoiceItemTotalGroup = value;
    }

}
