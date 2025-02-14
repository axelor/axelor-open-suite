
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;


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
 *         &lt;element name="Currency" type="{}CurrencyType"/>
 *         &lt;element name="PaymentDescription" type="{}PaymentDescriptionType"/>
 *         &lt;element name="Payable" type="{}YesNoType"/>
 *         &lt;element name="PayDueDate" type="{}DateType" minOccurs="0"/>
 *         &lt;element name="PaymentTotalSum" type="{}Decimal2FractionDigitsType"/>
 *         &lt;element name="PayerName" type="{}NormalTextType"/>
 *         &lt;element name="PaymentId" type="{}NormalTextType"/>
 *         &lt;element name="PayToAccount" type="{}AccountType"/>
 *         &lt;element name="PayToName" type="{}NormalTextType"/>
 *         &lt;element name="PayToBIC" type="{}BICType" minOccurs="0"/>
 *         &lt;element name="DirectDebitPayeeContractNumber" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="DirectDebitPayerNumber" type="{}ReferenceType" minOccurs="0"/>
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
        "currency",
        "paymentDescription",
        "payable",
        "payDueDate",
        "paymentTotalSum",
        "payerName",
        "paymentId",
        "payToAccount",
        "payToName",
        "payToBIC",
        "directDebitPayeeContractNumber",
        "directDebitPayerNumber"
})
@XmlRootElement(name = "PaymentInfo")
public class PaymentInfo {

    @XmlElement(name = "Currency", required = true)
    protected String currency;
    @XmlElement(name = "PaymentDescription", required = true)
    protected String paymentDescription;
    @XmlElement(name = "Payable", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String payable;
    @XmlElement(name = "PayDueDate")
    @XmlSchemaType(name = "date")
    protected String payDueDate;
    @XmlElement(name = "PaymentTotalSum", required = true)
    protected BigDecimal paymentTotalSum;
    @XmlElement(name = "PayerName", required = true)
    protected String payerName;
    @XmlElement(name = "PaymentId", required = true)
    protected String paymentId;
    @XmlElement(name = "PayToAccount", required = true)
    protected String payToAccount;
    @XmlElement(name = "PayToName", required = true)
    protected String payToName;
    @XmlElement(name = "PayToBIC")
    protected String payToBIC;
    @XmlElement(name = "DirectDebitPayeeContractNumber")
    protected String directDebitPayeeContractNumber;
    @XmlElement(name = "DirectDebitPayerNumber")
    protected String directDebitPayerNumber;

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
     * Gets the value of the paymentDescription property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPaymentDescription() {
        return paymentDescription;
    }

    /**
     * Sets the value of the paymentDescription property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPaymentDescription(String value) {
        this.paymentDescription = value;
    }

    /**
     * Gets the value of the payable property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPayable() {
        return payable;
    }

    /**
     * Sets the value of the payable property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPayable(String value) {
        this.payable = value;
    }

    /**
     * Gets the value of the payDueDate property.
     *
     * @return
     *     possible object is
     *     {@link LocalDate }
     *
     */
    public LocalDate getPayDueDate() {
        return LocalDate.parse(payDueDate);
    }

    /**
     * Sets the value of the payDueDate property.
     *
     * @param value
     *     allowed object is
     *     {@link LocalDate }
     *
     */
    public void setPayDueDate(LocalDate value) {
        this.payDueDate = value.toString();
    }

    /**
     * Gets the value of the paymentTotalSum property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getPaymentTotalSum() {
        return paymentTotalSum;
    }

    /**
     * Sets the value of the paymentTotalSum property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setPaymentTotalSum(BigDecimal value) {
        this.paymentTotalSum = value;
    }

    /**
     * Gets the value of the payerName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPayerName() {
        return payerName;
    }

    /**
     * Sets the value of the payerName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPayerName(String value) {
        this.payerName = value;
    }

    /**
     * Gets the value of the paymentId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPaymentId() {
        return paymentId;
    }

    /**
     * Sets the value of the paymentId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPaymentId(String value) {
        this.paymentId = value;
    }

    /**
     * Gets the value of the payToAccount property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPayToAccount() {
        return payToAccount;
    }

    /**
     * Sets the value of the payToAccount property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPayToAccount(String value) {
        this.payToAccount = value;
    }

    /**
     * Gets the value of the payToName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPayToName() {
        return payToName;
    }

    /**
     * Sets the value of the payToName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPayToName(String value) {
        this.payToName = value;
    }

    /**
     * Gets the value of the payToBIC property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPayToBIC() {
        return payToBIC;
    }

    /**
     * Sets the value of the payToBIC property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPayToBIC(String value) {
        this.payToBIC = value;
    }

    /**
     * Gets the value of the directDebitPayeeContractNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDirectDebitPayeeContractNumber() {
        return directDebitPayeeContractNumber;
    }

    /**
     * Sets the value of the directDebitPayeeContractNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDirectDebitPayeeContractNumber(String value) {
        this.directDebitPayeeContractNumber = value;
    }

    /**
     * Gets the value of the directDebitPayerNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDirectDebitPayerNumber() {
        return directDebitPayerNumber;
    }

    /**
     * Sets the value of the directDebitPayerNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDirectDebitPayerNumber(String value) {
        this.directDebitPayerNumber = value;
    }

}
