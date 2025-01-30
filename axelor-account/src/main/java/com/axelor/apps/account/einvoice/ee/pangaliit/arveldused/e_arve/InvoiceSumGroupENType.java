
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;


/**
 * <p>Java class for InvoiceSumGroupENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvoiceSumGroupENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PrepaidAmount" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="AllowanceSum" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="ChargeSum" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="InvoiceTotalVATSumInAccountingCurrency" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;>Decimal2FractionDigitsType">
 *                 &lt;attribute name="currency" use="required" type="{}CurrencyType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="InvoiceTotalSumWithoutVAT" type="{}Decimal2FractionDigitsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvoiceSumGroupENType", propOrder = {
    "prepaidAmount",
    "allowanceSum",
    "chargeSum",
    "invoiceTotalVATSumInAccountingCurrency",
    "invoiceTotalSumWithoutVAT"
})
public class InvoiceSumGroupENType {

    @XmlElement(name = "PrepaidAmount")
    protected BigDecimal prepaidAmount;
    @XmlElement(name = "AllowanceSum")
    protected BigDecimal allowanceSum;
    @XmlElement(name = "ChargeSum")
    protected BigDecimal chargeSum;
    @XmlElement(name = "InvoiceTotalVATSumInAccountingCurrency")
    protected InvoiceTotalVATSumInAccountingCurrency invoiceTotalVATSumInAccountingCurrency;
    @XmlElement(name = "InvoiceTotalSumWithoutVAT", required = true)
    protected BigDecimal invoiceTotalSumWithoutVAT;

    /**
     * Gets the value of the prepaidAmount property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getPrepaidAmount() {
        return prepaidAmount;
    }

    /**
     * Sets the value of the prepaidAmount property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setPrepaidAmount(BigDecimal value) {
        this.prepaidAmount = value;
    }

    /**
     * Gets the value of the allowanceSum property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getAllowanceSum() {
        return allowanceSum;
    }

    /**
     * Sets the value of the allowanceSum property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setAllowanceSum(BigDecimal value) {
        this.allowanceSum = value;
    }

    /**
     * Gets the value of the chargeSum property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getChargeSum() {
        return chargeSum;
    }

    /**
     * Sets the value of the chargeSum property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setChargeSum(BigDecimal value) {
        this.chargeSum = value;
    }

    /**
     * Gets the value of the invoiceTotalVATSumInAccountingCurrency property.
     *
     * @return
     *     possible object is
     *     {@link InvoiceTotalVATSumInAccountingCurrency }
     *
     */
    public InvoiceTotalVATSumInAccountingCurrency getInvoiceTotalVATSumInAccountingCurrency() {
        return invoiceTotalVATSumInAccountingCurrency;
    }

    /**
     * Sets the value of the invoiceTotalVATSumInAccountingCurrency property.
     *
     * @param value
     *     allowed object is
     *     {@link InvoiceTotalVATSumInAccountingCurrency }
     *
     */
    public void setInvoiceTotalVATSumInAccountingCurrency(InvoiceTotalVATSumInAccountingCurrency value) {
        this.invoiceTotalVATSumInAccountingCurrency = value;
    }

    /**
     * Gets the value of the invoiceTotalSumWithoutVAT property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getInvoiceTotalSumWithoutVAT() {
        return invoiceTotalSumWithoutVAT;
    }

    /**
     * Sets the value of the invoiceTotalSumWithoutVAT property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setInvoiceTotalSumWithoutVAT(BigDecimal value) {
        this.invoiceTotalSumWithoutVAT = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;>Decimal2FractionDigitsType">
     *       &lt;attribute name="currency" use="required" type="{}CurrencyType" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class InvoiceTotalVATSumInAccountingCurrency {

        @XmlValue
        protected BigDecimal value;
        @XmlAttribute(name = "currency", required = true)
        protected String currency;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setValue(BigDecimal value) {
            this.value = value;
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

    }

}
