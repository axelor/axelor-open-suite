
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
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
 *         &lt;element name="Balance" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="BalanceDate" type="{}DateType" minOccurs="0"/>
 *                   &lt;element name="BalanceBegin" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *                   &lt;element name="Inbound" type="{}Decimal2FractionDigitsType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="Outbound" type="{}Decimal2FractionDigitsType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="BalanceEnd" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="InvoiceSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="PenaltySum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="Addition" type="{}AdditionRecord" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Rounding" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="VAT" type="{}VATRecord" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="TotalVATSum" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="TotalSum" type="{}Decimal2FractionDigitsType"/>
 *         &lt;element name="TotalToPay" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="Currency" type="{}CurrencyType" minOccurs="0"/>
 *         &lt;element name="Accounting" type="{}AccountingRecord" minOccurs="0"/>
 *         &lt;element name="Extension" type="{}ExtensionRecord" maxOccurs="unbounded" minOccurs="0"/>
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
    "balance",
    "invoiceSum",
    "penaltySum",
    "addition",
    "rounding",
    "vat",
    "totalVATSum",
    "totalSum",
    "totalToPay",
    "currency",
    "accounting",
    "extension"
})
@XmlRootElement(name = "InvoiceSumGroup")
public class InvoiceSumGroup {

    @XmlElement(name = "Balance")
    protected Balance balance;
    @XmlElement(name = "InvoiceSum")
    protected BigDecimal invoiceSum;
    @XmlElement(name = "PenaltySum")
    protected BigDecimal penaltySum;
    @XmlElement(name = "Addition")
    protected List<AdditionRecord> addition;
    @XmlElement(name = "Rounding")
    protected BigDecimal rounding;
    @XmlElement(name = "VAT")
    protected List<VATRecord> vat;
    @XmlElement(name = "TotalVATSum")
    protected BigDecimal totalVATSum;
    @XmlElement(name = "TotalSum", required = true)
    protected BigDecimal totalSum;
    @XmlElement(name = "TotalToPay")
    protected BigDecimal totalToPay;
    @XmlElement(name = "Currency")
    protected String currency;
    @XmlElement(name = "Accounting")
    protected AccountingRecord accounting;
    @XmlElement(name = "Extension")
    protected List<ExtensionRecord> extension;

    /**
     * Gets the value of the balance property.
     *
     * @return
     *     possible object is
     *     {@link Balance }
     *
     */
    public Balance getBalance() {
        return balance;
    }

    /**
     * Sets the value of the balance property.
     *
     * @param value
     *     allowed object is
     *     {@link Balance }
     *
     */
    public void setBalance(Balance value) {
        this.balance = value;
    }

    /**
     * Gets the value of the invoiceSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getInvoiceSum() {
        return invoiceSum;
    }

    /**
     * Sets the value of the invoiceSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setInvoiceSum(BigDecimal value) {
        this.invoiceSum = value;
    }

    /**
     * Gets the value of the penaltySum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPenaltySum() {
        return penaltySum;
    }

    /**
     * Sets the value of the penaltySum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPenaltySum(BigDecimal value) {
        this.penaltySum = value;
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
     * Gets the value of the rounding property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRounding() {
        return rounding;
    }

    /**
     * Sets the value of the rounding property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRounding(BigDecimal value) {
        this.rounding = value;
    }

    /**
     * Gets the value of the vat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVAT().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VATRecord }
     * 
     * 
     */
    public List<VATRecord> getVAT() {
        if (vat == null) {
            vat = new ArrayList<VATRecord>();
        }
        return this.vat;
    }

    /**
     * Gets the value of the totalVATSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalVATSum() {
        return totalVATSum;
    }

    /**
     * Sets the value of the totalVATSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalVATSum(BigDecimal value) {
        this.totalVATSum = value;
    }

    /**
     * Gets the value of the totalSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalSum() {
        return totalSum;
    }

    /**
     * Sets the value of the totalSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalSum(BigDecimal value) {
        this.totalSum = value;
    }

    /**
     * Gets the value of the totalToPay property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalToPay() {
        return totalToPay;
    }

    /**
     * Sets the value of the totalToPay property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalToPay(BigDecimal value) {
        this.totalToPay = value;
    }

    /**
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="BalanceDate" type="{}DateType" minOccurs="0"/>
     *         &lt;element name="BalanceBegin" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
     *         &lt;element name="Inbound" type="{}Decimal2FractionDigitsType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="Outbound" type="{}Decimal2FractionDigitsType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="BalanceEnd" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
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
        "balanceDate",
        "balanceBegin",
        "inbound",
        "outbound",
        "balanceEnd"
    })
    public static class Balance {

        @XmlElement(name = "BalanceDate")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar balanceDate;
        @XmlElement(name = "BalanceBegin")
        protected BigDecimal balanceBegin;
        @XmlElement(name = "Inbound")
        protected List<BigDecimal> inbound;
        @XmlElement(name = "Outbound")
        protected List<BigDecimal> outbound;
        @XmlElement(name = "BalanceEnd")
        protected BigDecimal balanceEnd;

        /**
         * Gets the value of the balanceDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getBalanceDate() {
            return balanceDate;
        }

        /**
         * Sets the value of the balanceDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setBalanceDate(XMLGregorianCalendar value) {
            this.balanceDate = value;
        }

        /**
         * Gets the value of the balanceBegin property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getBalanceBegin() {
            return balanceBegin;
        }

        /**
         * Sets the value of the balanceBegin property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setBalanceBegin(BigDecimal value) {
            this.balanceBegin = value;
        }

        /**
         * Gets the value of the inbound property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the inbound property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInbound().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BigDecimal }
         * 
         * 
         */
        public List<BigDecimal> getInbound() {
            if (inbound == null) {
                inbound = new ArrayList<BigDecimal>();
            }
            return this.inbound;
        }

        /**
         * Gets the value of the outbound property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the outbound property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOutbound().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BigDecimal }
         * 
         * 
         */
        public List<BigDecimal> getOutbound() {
            if (outbound == null) {
                outbound = new ArrayList<BigDecimal>();
            }
            return this.outbound;
        }

        /**
         * Gets the value of the balanceEnd property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getBalanceEnd() {
            return balanceEnd;
        }

        /**
         * Sets the value of the balanceEnd property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setBalanceEnd(BigDecimal value) {
            this.balanceEnd = value;
        }

    }

}
