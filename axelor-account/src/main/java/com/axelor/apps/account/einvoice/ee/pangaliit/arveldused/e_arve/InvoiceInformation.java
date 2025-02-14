
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 *         &lt;element name="Type">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SourceInvoice" type="{}ShortTextType" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="type" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *                       &lt;pattern value="DEB"/>
 *                       &lt;pattern value="CRE"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="FactorContractNumber" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="ContractNumber" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="DocumentName" type="{}NormalTextType"/>
 *         &lt;element name="InvoiceNumber" type="{}NormalTextType"/>
 *         &lt;element name="InvoiceContentCode" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="InvoiceContentText" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="PaymentReferenceNumber" type="{}ReferenceType" minOccurs="0"/>
 *         &lt;element name="PaymentMethod" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="InvoiceDate" type="{}DateType"/>
 *         &lt;element name="DueDate" type="{}DateType" minOccurs="0"/>
 *         &lt;element name="PaymentTerm" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="FineRatePerDay" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="Period" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="PeriodName" type="{}NormalTextType" minOccurs="0"/>
 *                   &lt;element name="StartDate" type="{}DateType" minOccurs="0"/>
 *                   &lt;element name="EndDate" type="{}DateType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="InvoiceDeliverer" type="{}ContactDataRecord" minOccurs="0"/>
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
        "type",
        "factorContractNumber",
        "contractNumber",
        "documentName",
        "invoiceNumber",
        "invoiceContentCode",
        "invoiceContentText",
        "paymentReferenceNumber",
        "paymentMethod",
        "invoiceDate",
        "dueDate",
        "paymentTerm",
        "fineRatePerDay",
        "period",
        "invoiceDeliverer",
        "extension"
})
@XmlRootElement(name = "InvoiceInformation")
public class InvoiceInformation {

    @XmlElement(name = "Type", required = true)
    protected Type type;
    @XmlElement(name = "FactorContractNumber")
    protected String factorContractNumber;
    @XmlElement(name = "ContractNumber")
    protected String contractNumber;
    @XmlElement(name = "DocumentName", required = true)
    protected String documentName;
    @XmlElement(name = "InvoiceNumber", required = true)
    protected String invoiceNumber;
    @XmlElement(name = "InvoiceContentCode")
    protected String invoiceContentCode;
    @XmlElement(name = "InvoiceContentText")
    protected String invoiceContentText;
    @XmlElement(name = "PaymentReferenceNumber")
    protected String paymentReferenceNumber;
    @XmlElement(name = "PaymentMethod")
    protected String paymentMethod;
    @XmlElement(name = "InvoiceDate", required = true)
    @XmlSchemaType(name = "date")
    protected String invoiceDate;
    @XmlElement(name = "DueDate")
    @XmlSchemaType(name = "date")
    protected String dueDate;
    @XmlElement(name = "PaymentTerm")
    protected String paymentTerm;
    @XmlElement(name = "FineRatePerDay")
    protected BigDecimal fineRatePerDay;
    @XmlElement(name = "Period")
    protected Period period;
    @XmlElement(name = "InvoiceDeliverer")
    protected ContactDataRecord invoiceDeliverer;
    @XmlElement(name = "Extension")
    protected List<ExtensionRecord> extension;

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link Type }
     *
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link Type }
     *
     */
    public void setType(Type value) {
        this.type = value;
    }

    /**
     * Gets the value of the factorContractNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFactorContractNumber() {
        return factorContractNumber;
    }

    /**
     * Sets the value of the factorContractNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFactorContractNumber(String value) {
        this.factorContractNumber = value;
    }

    /**
     * Gets the value of the contractNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContractNumber() {
        return contractNumber;
    }

    /**
     * Sets the value of the contractNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContractNumber(String value) {
        this.contractNumber = value;
    }

    /**
     * Gets the value of the documentName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * Sets the value of the documentName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDocumentName(String value) {
        this.documentName = value;
    }

    /**
     * Gets the value of the invoiceNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    /**
     * Sets the value of the invoiceNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInvoiceNumber(String value) {
        this.invoiceNumber = value;
    }

    /**
     * Gets the value of the invoiceContentCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInvoiceContentCode() {
        return invoiceContentCode;
    }

    /**
     * Sets the value of the invoiceContentCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInvoiceContentCode(String value) {
        this.invoiceContentCode = value;
    }

    /**
     * Gets the value of the invoiceContentText property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInvoiceContentText() {
        return invoiceContentText;
    }

    /**
     * Sets the value of the invoiceContentText property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInvoiceContentText(String value) {
        this.invoiceContentText = value;
    }

    /**
     * Gets the value of the paymentReferenceNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPaymentReferenceNumber() {
        return paymentReferenceNumber;
    }

    /**
     * Sets the value of the paymentReferenceNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPaymentReferenceNumber(String value) {
        this.paymentReferenceNumber = value;
    }

    /**
     * Gets the value of the paymentMethod property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets the value of the paymentMethod property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    /**
     * Gets the value of the invoiceDate property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInvoiceDate() {
        return invoiceDate;
    }

    /**
     * Sets the value of the invoiceDate property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInvoiceDate(LocalDate value) {
        this.invoiceDate = value.toString();
    }

    /**
     * Gets the value of the dueDate property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public LocalDate getDueDate() {
        return LocalDate.parse(dueDate);
    }

    /**
     * Sets the value of the dueDate property.
     *
     * @param value
     *     allowed object is
     *     {@link LocalDate }
     *
     */
    public void setDueDate(LocalDate value) {
        this.dueDate = value.toString();
    }

    /**
     * Gets the value of the paymentTerm property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPaymentTerm() {
        return paymentTerm;
    }

    /**
     * Sets the value of the paymentTerm property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPaymentTerm(String value) {
        this.paymentTerm = value;
    }

    /**
     * Gets the value of the fineRatePerDay property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getFineRatePerDay() {
        return fineRatePerDay;
    }

    /**
     * Sets the value of the fineRatePerDay property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setFineRatePerDay(BigDecimal value) {
        this.fineRatePerDay = value;
    }

    /**
     * Gets the value of the period property.
     *
     * @return
     *     possible object is
     *     {@link Period }
     *
     */
    public Period getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     *
     * @param value
     *     allowed object is
     *     {@link Period }
     *
     */
    public void setPeriod(Period value) {
        this.period = value;
    }

    /**
     * Gets the value of the invoiceDeliverer property.
     *
     * @return
     *     possible object is
     *     {@link ContactDataRecord }
     *
     */
    public ContactDataRecord getInvoiceDeliverer() {
        return invoiceDeliverer;
    }

    /**
     * Sets the value of the invoiceDeliverer property.
     *
     * @param value
     *     allowed object is
     *     {@link ContactDataRecord }
     *
     */
    public void setInvoiceDeliverer(ContactDataRecord value) {
        this.invoiceDeliverer = value;
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
     *         &lt;element name="PeriodName" type="{}NormalTextType" minOccurs="0"/>
     *         &lt;element name="StartDate" type="{}DateType" minOccurs="0"/>
     *         &lt;element name="EndDate" type="{}DateType" minOccurs="0"/>
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
            "periodName",
            "startDate",
            "endDate"
    })
    public static class Period {

        @XmlElement(name = "PeriodName")
        protected String periodName;
        @XmlElement(name = "StartDate")
        @XmlSchemaType(name = "date")
        protected String startDate;
        @XmlElement(name = "EndDate")
        @XmlSchemaType(name = "date")
        protected String endDate;

        /**
         * Gets the value of the periodName property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getPeriodName() {
            return periodName;
        }

        /**
         * Sets the value of the periodName property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setPeriodName(String value) {
            this.periodName = value;
        }

        /**
         * Gets the value of the startDate property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getStartDate() {
            return startDate;
        }

        /**
         * Sets the value of the startDate property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setStartDate(LocalDate value) {
            this.startDate = value.toString();
        }

        /**
         * Gets the value of the endDate property.
         *
         * @return
         *     possible object is
         *     {@link LocalDate }
         *
         */
        public LocalDate getEndDate() {
            return LocalDate.parse(endDate);
        }

        /**
         * Sets the value of the endDate property.
         *
         * @param value
         *     allowed object is
         *     {@link LocalDate }
         *
         */
        public void setEndDate(LocalDate value) {
            this.endDate = value.toString();
        }

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
     *         &lt;element name="SourceInvoice" type="{}ShortTextType" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="type" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
     *             &lt;pattern value="DEB"/>
     *             &lt;pattern value="CRE"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "sourceInvoice"
    })
    public static class Type {

        @XmlElement(name = "SourceInvoice")
        protected String sourceInvoice;
        @XmlAttribute(name = "type", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String type;

//        public Type() {
//        }
//
//        public Type(String type) {
//            this.type = type;
//        }

        /**
         * Gets the value of the sourceInvoice property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getSourceInvoice() {
            return sourceInvoice;
        }

        /**
         * Sets the value of the sourceInvoice property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setSourceInvoice(String value) {
            this.sourceInvoice = value;
        }

        /**
         * Gets the value of the type property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setType(String value) {
            this.type = value;
        }

    }

}
