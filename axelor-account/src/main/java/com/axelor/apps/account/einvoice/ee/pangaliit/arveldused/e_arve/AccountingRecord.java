
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for AccountingRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AccountingRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Description" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="JournalEntry" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="GeneralLedger" type="{}ShortTextType" minOccurs="0"/>
 *                   &lt;element name="GeneralLedgerDetail" type="{}ShortTextType" minOccurs="0"/>
 *                   &lt;element name="CostObjective" type="{}ShortTextType" minOccurs="0"/>
 *                   &lt;element name="Sum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *                   &lt;element name="VatSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *                   &lt;element name="VatRate" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PartnerCode" type="{}EncodingType" minOccurs="0"/>
 *         &lt;element name="BusinessCode" type="{}EncodingType" minOccurs="0"/>
 *         &lt;element name="SourceCode" type="{}EncodingType" minOccurs="0"/>
 *         &lt;element name="CashFlowCode" type="{}EncodingType" minOccurs="0"/>
 *         &lt;element name="ClassificatorCode" type="{}NormalTextType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountingRecord", propOrder = {
    "description",
    "journalEntry",
    "partnerCode",
    "businessCode",
    "sourceCode",
    "cashFlowCode",
    "classificatorCode"
})
public class AccountingRecord {

    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "JournalEntry", required = true)
    protected List<JournalEntry> journalEntry;
    @XmlElement(name = "PartnerCode")
    protected String partnerCode;
    @XmlElement(name = "BusinessCode")
    protected String businessCode;
    @XmlElement(name = "SourceCode")
    protected String sourceCode;
    @XmlElement(name = "CashFlowCode")
    protected String cashFlowCode;
    @XmlElement(name = "ClassificatorCode")
    protected String classificatorCode;

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the journalEntry property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the journalEntry property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJournalEntry().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JournalEntry }
     *
     *
     */
    public List<JournalEntry> getJournalEntry() {
        if (journalEntry == null) {
            journalEntry = new ArrayList<JournalEntry>();
        }
        return this.journalEntry;
    }

    /**
     * Gets the value of the partnerCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartnerCode() {
        return partnerCode;
    }

    /**
     * Sets the value of the partnerCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartnerCode(String value) {
        this.partnerCode = value;
    }

    /**
     * Gets the value of the businessCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusinessCode() {
        return businessCode;
    }

    /**
     * Sets the value of the businessCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusinessCode(String value) {
        this.businessCode = value;
    }

    /**
     * Gets the value of the sourceCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * Sets the value of the sourceCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceCode(String value) {
        this.sourceCode = value;
    }

    /**
     * Gets the value of the cashFlowCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCashFlowCode() {
        return cashFlowCode;
    }

    /**
     * Sets the value of the cashFlowCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCashFlowCode(String value) {
        this.cashFlowCode = value;
    }

    /**
     * Gets the value of the classificatorCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassificatorCode() {
        return classificatorCode;
    }

    /**
     * Sets the value of the classificatorCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassificatorCode(String value) {
        this.classificatorCode = value;
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
     *         &lt;element name="GeneralLedger" type="{}ShortTextType" minOccurs="0"/>
     *         &lt;element name="GeneralLedgerDetail" type="{}ShortTextType" minOccurs="0"/>
     *         &lt;element name="CostObjective" type="{}ShortTextType" minOccurs="0"/>
     *         &lt;element name="Sum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
     *         &lt;element name="VatSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
     *         &lt;element name="VatRate" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
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
        "generalLedger",
        "generalLedgerDetail",
        "costObjective",
        "sum",
        "vatSum",
        "vatRate"
    })
    public static class JournalEntry {

        @XmlElement(name = "GeneralLedger")
        protected String generalLedger;
        @XmlElement(name = "GeneralLedgerDetail")
        protected String generalLedgerDetail;
        @XmlElement(name = "CostObjective")
        protected String costObjective;
        @XmlElement(name = "Sum")
        protected BigDecimal sum;
        @XmlElement(name = "VatSum")
        protected BigDecimal vatSum;
        @XmlElement(name = "VatRate")
        protected BigDecimal vatRate;

        /**
         * Gets the value of the generalLedger property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getGeneralLedger() {
            return generalLedger;
        }

        /**
         * Sets the value of the generalLedger property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setGeneralLedger(String value) {
            this.generalLedger = value;
        }

        /**
         * Gets the value of the generalLedgerDetail property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getGeneralLedgerDetail() {
            return generalLedgerDetail;
        }

        /**
         * Sets the value of the generalLedgerDetail property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setGeneralLedgerDetail(String value) {
            this.generalLedgerDetail = value;
        }

        /**
         * Gets the value of the costObjective property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCostObjective() {
            return costObjective;
        }

        /**
         * Sets the value of the costObjective property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCostObjective(String value) {
            this.costObjective = value;
        }

        /**
         * Gets the value of the sum property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getSum() {
            return sum;
        }

        /**
         * Sets the value of the sum property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setSum(BigDecimal value) {
            this.sum = value;
        }

        /**
         * Gets the value of the vatSum property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getVatSum() {
            return vatSum;
        }

        /**
         * Sets the value of the vatSum property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setVatSum(BigDecimal value) {
            this.vatSum = value;
        }

        /**
         * Gets the value of the vatRate property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getVatRate() {
            return vatRate;
        }

        /**
         * Sets the value of the vatRate property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setVatRate(BigDecimal value) {
            this.vatRate = value;
        }

    }

}
