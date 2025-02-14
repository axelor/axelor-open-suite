
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for PaymentInfoENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PaymentInfoENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PaymentMethodCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *         &lt;element name="PaymentCardInfo" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="PrimaryAccountNumber" type="{}LongTextType"/>
 *                   &lt;element name="PaymentCardHolderName" type="{}LongTextType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PaymentAccountName" type="{}LongTextType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentInfoENType", propOrder = {
    "paymentMethodCode",
    "paymentCardInfo",
    "paymentAccountName"
})
public class PaymentInfoENType {

    @XmlElement(name = "PaymentMethodCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String paymentMethodCode;
    @XmlElement(name = "PaymentCardInfo")
    protected PaymentCardInfo paymentCardInfo;
    @XmlElement(name = "PaymentAccountName")
    protected String paymentAccountName;

    /**
     * Gets the value of the paymentMethodCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    /**
     * Sets the value of the paymentMethodCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPaymentMethodCode(String value) {
        this.paymentMethodCode = value;
    }

    /**
     * Gets the value of the paymentCardInfo property.
     *
     * @return
     *     possible object is
     *     {@link PaymentCardInfo }
     *
     */
    public PaymentCardInfo getPaymentCardInfo() {
        return paymentCardInfo;
    }

    /**
     * Sets the value of the paymentCardInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link PaymentCardInfo }
     *
     */
    public void setPaymentCardInfo(PaymentCardInfo value) {
        this.paymentCardInfo = value;
    }

    /**
     * Gets the value of the paymentAccountName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentAccountName() {
        return paymentAccountName;
    }

    /**
     * Sets the value of the paymentAccountName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentAccountName(String value) {
        this.paymentAccountName = value;
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
     *         &lt;element name="PrimaryAccountNumber" type="{}LongTextType"/>
     *         &lt;element name="PaymentCardHolderName" type="{}LongTextType"/>
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
        "primaryAccountNumber",
        "paymentCardHolderName"
    })
    public static class PaymentCardInfo {

        @XmlElement(name = "PrimaryAccountNumber", required = true)
        protected String primaryAccountNumber;
        @XmlElement(name = "PaymentCardHolderName", required = true)
        protected String paymentCardHolderName;

        /**
         * Gets the value of the primaryAccountNumber property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPrimaryAccountNumber() {
            return primaryAccountNumber;
        }

        /**
         * Sets the value of the primaryAccountNumber property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPrimaryAccountNumber(String value) {
            this.primaryAccountNumber = value;
        }

        /**
         * Gets the value of the paymentCardHolderName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPaymentCardHolderName() {
            return paymentCardHolderName;
        }

        /**
         * Sets the value of the paymentCardHolderName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPaymentCardHolderName(String value) {
            this.paymentCardHolderName = value;
        }

    }

}
