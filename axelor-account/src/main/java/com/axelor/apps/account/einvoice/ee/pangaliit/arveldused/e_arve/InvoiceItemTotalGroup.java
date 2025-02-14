
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
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
 *         &lt;element name="InvoiceItemTotalDescription" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="Extension" type="{}ExtensionRecord" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Accounting" type="{}AccountingRecord" minOccurs="0"/>
 *         &lt;element name="InvoiceItemTotalAmount" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="InvoiceItemTotalSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="Addition" type="{}AdditionRecord" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="VAT" type="{}VATRecord" minOccurs="0"/>
 *         &lt;element name="InvoiceItemTotal" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
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
    "invoiceItemTotalDescription",
    "extension",
    "accounting",
    "invoiceItemTotalAmount",
    "invoiceItemTotalSum",
    "addition",
    "vat",
    "invoiceItemTotal"
})
@XmlRootElement(name = "InvoiceItemTotalGroup")
public class InvoiceItemTotalGroup {

    @XmlElement(name = "InvoiceItemTotalDescription")
    protected String invoiceItemTotalDescription;
    @XmlElement(name = "Extension")
    protected List<ExtensionRecord> extension;
    @XmlElement(name = "Accounting")
    protected AccountingRecord accounting;
    @XmlElement(name = "InvoiceItemTotalAmount")
    protected BigDecimal invoiceItemTotalAmount;
    @XmlElement(name = "InvoiceItemTotalSum")
    protected BigDecimal invoiceItemTotalSum;
    @XmlElement(name = "Addition")
    protected List<AdditionRecord> addition;
    @XmlElement(name = "VAT")
    protected VATRecord vat;
    @XmlElement(name = "InvoiceItemTotal")
    protected BigDecimal invoiceItemTotal;

    /**
     * Gets the value of the invoiceItemTotalDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvoiceItemTotalDescription() {
        return invoiceItemTotalDescription;
    }

    /**
     * Sets the value of the invoiceItemTotalDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvoiceItemTotalDescription(String value) {
        this.invoiceItemTotalDescription = value;
    }

    /**
     * Gets the value of the extension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExtensionRecord }
     * 
     * 
     */
    public List<ExtensionRecord> getExtension() {
        if (extension == null) {
            extension = new ArrayList<ExtensionRecord>();
        }
        return this.extension;
    }

    /**
     * Gets the value of the accounting property.
     * 
     * @return
     *     possible object is
     *     {@link AccountingRecord }
     *     
     */
    public AccountingRecord getAccounting() {
        return accounting;
    }

    /**
     * Sets the value of the accounting property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountingRecord }
     *     
     */
    public void setAccounting(AccountingRecord value) {
        this.accounting = value;
    }

    /**
     * Gets the value of the invoiceItemTotalAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getInvoiceItemTotalAmount() {
        return invoiceItemTotalAmount;
    }

    /**
     * Sets the value of the invoiceItemTotalAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setInvoiceItemTotalAmount(BigDecimal value) {
        this.invoiceItemTotalAmount = value;
    }

    /**
     * Gets the value of the invoiceItemTotalSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getInvoiceItemTotalSum() {
        return invoiceItemTotalSum;
    }

    /**
     * Sets the value of the invoiceItemTotalSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setInvoiceItemTotalSum(BigDecimal value) {
        this.invoiceItemTotalSum = value;
    }

    /**
     * Gets the value of the addition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionRecord }
     * 
     * 
     */
    public List<AdditionRecord> getAddition() {
        if (addition == null) {
            addition = new ArrayList<AdditionRecord>();
        }
        return this.addition;
    }

    /**
     * Gets the value of the vat property.
     * 
     * @return
     *     possible object is
     *     {@link VATRecord }
     *     
     */
    public VATRecord getVAT() {
        return vat;
    }

    /**
     * Sets the value of the vat property.
     * 
     * @param value
     *     allowed object is
     *     {@link VATRecord }
     *     
     */
    public void setVAT(VATRecord value) {
        this.vat = value;
    }

    /**
     * Gets the value of the invoiceItemTotal property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getInvoiceItemTotal() {
        return invoiceItemTotal;
    }

    /**
     * Sets the value of the invoiceItemTotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setInvoiceItemTotal(BigDecimal value) {
        this.invoiceItemTotal = value;
    }

}
