
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for InvoiceInformationENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvoiceInformationENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InvoiceTypeCode" type="{}ShortTextType"/>
 *         &lt;element name="VATPointDate" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="VATPointDate" type="{}DateType" minOccurs="0"/>
 *                   &lt;element name="VATPointDateCode" type="{}ShortTextType" minOccurs="0"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ProjectRef" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ObjectId" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
 *                 &lt;attribute name="schemeId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PurchaseOrderRef" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="SalesOrderRef" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ReceivingAdviceRef" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="DespatchAdviceRef" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="TenderRef" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="ActualDeliveryDate" type="{}DateType" minOccurs="0"/>
 *         &lt;element name="PrecedingInvoice" type="{}AdditionalDocumentRecordEN" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AdditionalDocument" type="{}AdditionalDocumentRecordEN" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ProcessControl">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="BusinessProcessType" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *                   &lt;element name="SpecificationId" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="InvoiceNote" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SubjectCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *                   &lt;element name="Note" type="{}LongTextType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvoiceInformationENType", propOrder = {
    "invoiceTypeCode",
    "vatPointDate",
    "projectRef",
    "objectId",
    "purchaseOrderRef",
    "salesOrderRef",
    "receivingAdviceRef",
    "despatchAdviceRef",
    "tenderRef",
    "actualDeliveryDate",
    "precedingInvoice",
    "additionalDocument",
    "processControl",
    "invoiceNote"
})
public class InvoiceInformationENType {

    @XmlElement(name = "InvoiceTypeCode", required = true)
    protected String invoiceTypeCode;
    @XmlElement(name = "VATPointDate")
    protected VATPointDate vatPointDate;
    @XmlElement(name = "ProjectRef")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String projectRef;
    @XmlElement(name = "ObjectId")
    protected ObjectId objectId;
    @XmlElement(name = "PurchaseOrderRef")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String purchaseOrderRef;
    @XmlElement(name = "SalesOrderRef")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String salesOrderRef;
    @XmlElement(name = "ReceivingAdviceRef")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String receivingAdviceRef;
    @XmlElement(name = "DespatchAdviceRef")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String despatchAdviceRef;
    @XmlElement(name = "TenderRef")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String tenderRef;
    @XmlElement(name = "ActualDeliveryDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar actualDeliveryDate;
    @XmlElement(name = "PrecedingInvoice")
    protected List<AdditionalDocumentRecordEN> precedingInvoice;
    @XmlElement(name = "AdditionalDocument")
    protected List<AdditionalDocumentRecordEN> additionalDocument;
    @XmlElement(name = "ProcessControl", required = true)
    protected ProcessControl processControl;
    @XmlElement(name = "InvoiceNote")
    protected List<InvoiceNote> invoiceNote;

    /**
     * Gets the value of the invoiceTypeCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInvoiceTypeCode() {
        return invoiceTypeCode;
    }

    /**
     * Sets the value of the invoiceTypeCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInvoiceTypeCode(String value) {
        this.invoiceTypeCode = value;
    }

    /**
     * Gets the value of the vatPointDate property.
     *
     * @return
     *     possible object is
     *     {@link VATPointDate }
     *
     */
    public VATPointDate getVATPointDate() {
        return vatPointDate;
    }

    /**
     * Sets the value of the vatPointDate property.
     *
     * @param value
     *     allowed object is
     *     {@link VATPointDate }
     *
     */
    public void setVATPointDate(VATPointDate value) {
        this.vatPointDate = value;
    }

    /**
     * Gets the value of the projectRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getProjectRef() {
        return projectRef;
    }

    /**
     * Sets the value of the projectRef property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setProjectRef(String value) {
        this.projectRef = value;
    }

    /**
     * Gets the value of the objectId property.
     *
     * @return
     *     possible object is
     *     {@link ObjectId }
     *
     */
    public ObjectId getObjectId() {
        return objectId;
    }

    /**
     * Sets the value of the objectId property.
     *
     * @param value
     *     allowed object is
     *     {@link ObjectId }
     *
     */
    public void setObjectId(ObjectId value) {
        this.objectId = value;
    }

    /**
     * Gets the value of the purchaseOrderRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPurchaseOrderRef() {
        return purchaseOrderRef;
    }

    /**
     * Sets the value of the purchaseOrderRef property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPurchaseOrderRef(String value) {
        this.purchaseOrderRef = value;
    }

    /**
     * Gets the value of the salesOrderRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSalesOrderRef() {
        return salesOrderRef;
    }

    /**
     * Sets the value of the salesOrderRef property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSalesOrderRef(String value) {
        this.salesOrderRef = value;
    }

    /**
     * Gets the value of the receivingAdviceRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReceivingAdviceRef() {
        return receivingAdviceRef;
    }

    /**
     * Sets the value of the receivingAdviceRef property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReceivingAdviceRef(String value) {
        this.receivingAdviceRef = value;
    }

    /**
     * Gets the value of the despatchAdviceRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDespatchAdviceRef() {
        return despatchAdviceRef;
    }

    /**
     * Sets the value of the despatchAdviceRef property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDespatchAdviceRef(String value) {
        this.despatchAdviceRef = value;
    }

    /**
     * Gets the value of the tenderRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTenderRef() {
        return tenderRef;
    }

    /**
     * Sets the value of the tenderRef property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTenderRef(String value) {
        this.tenderRef = value;
    }

    /**
     * Gets the value of the actualDeliveryDate property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    /**
     * Sets the value of the actualDeliveryDate property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setActualDeliveryDate(XMLGregorianCalendar value) {
        this.actualDeliveryDate = value;
    }

    /**
     * Gets the value of the precedingInvoice property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the precedingInvoice property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrecedingInvoice().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionalDocumentRecordEN }
     *
     *
     */
    public List<AdditionalDocumentRecordEN> getPrecedingInvoice() {
        if (precedingInvoice == null) {
            precedingInvoice = new ArrayList<AdditionalDocumentRecordEN>();
        }
        return this.precedingInvoice;
    }

    /**
     * Gets the value of the additionalDocument property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additionalDocument property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditionalDocument().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionalDocumentRecordEN }
     *
     *
     */
    public List<AdditionalDocumentRecordEN> getAdditionalDocument() {
        if (additionalDocument == null) {
            additionalDocument = new ArrayList<AdditionalDocumentRecordEN>();
        }
        return this.additionalDocument;
    }

    /**
     * Gets the value of the processControl property.
     *
     * @return
     *     possible object is
     *     {@link ProcessControl }
     *
     */
    public ProcessControl getProcessControl() {
        return processControl;
    }

    /**
     * Sets the value of the processControl property.
     *
     * @param value
     *     allowed object is
     *     {@link ProcessControl }
     *
     */
    public void setProcessControl(ProcessControl value) {
        this.processControl = value;
    }

    /**
     * Gets the value of the invoiceNote property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the invoiceNote property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvoiceNote().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InvoiceNote }
     *
     *
     */
    public List<InvoiceNote> getInvoiceNote() {
        if (invoiceNote == null) {
            invoiceNote = new ArrayList<InvoiceNote>();
        }
        return this.invoiceNote;
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
     *         &lt;element name="SubjectCode" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
     *         &lt;element name="Note" type="{}LongTextType"/>
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
        "subjectCode",
        "note"
    })
    public static class InvoiceNote {

        @XmlElement(name = "SubjectCode")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String subjectCode;
        @XmlElement(name = "Note", required = true)
        protected String note;

        /**
         * Gets the value of the subjectCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubjectCode() {
            return subjectCode;
        }

        /**
         * Sets the value of the subjectCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubjectCode(String value) {
            this.subjectCode = value;
        }

        /**
         * Gets the value of the note property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNote() {
            return note;
        }

        /**
         * Sets the value of the note property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNote(String value) {
            this.note = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
     *       &lt;attribute name="schemeId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
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
    public static class ObjectId {

        @XmlValue
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String value;
        @XmlAttribute(name = "schemeId")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String schemeId;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the schemeId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSchemeId() {
            return schemeId;
        }

        /**
         * Sets the value of the schemeId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSchemeId(String value) {
            this.schemeId = value;
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
     *         &lt;element name="BusinessProcessType" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
     *         &lt;element name="SpecificationId" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
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
        "businessProcessType",
        "specificationId"
    })
    public static class ProcessControl {

        @XmlElement(name = "BusinessProcessType", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String businessProcessType;
        @XmlElement(name = "SpecificationId", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String specificationId;

        /**
         * Gets the value of the businessProcessType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBusinessProcessType() {
            return businessProcessType;
        }

        /**
         * Sets the value of the businessProcessType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBusinessProcessType(String value) {
            this.businessProcessType = value;
        }

        /**
         * Gets the value of the specificationId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSpecificationId() {
            return specificationId;
        }

        /**
         * Sets the value of the specificationId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSpecificationId(String value) {
            this.specificationId = value;
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
     *       &lt;choice>
     *         &lt;element name="VATPointDate" type="{}DateType" minOccurs="0"/>
     *         &lt;element name="VATPointDateCode" type="{}ShortTextType" minOccurs="0"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "vatPointDate",
        "vatPointDateCode"
    })
    public static class VATPointDate {

        @XmlElement(name = "VATPointDate")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar vatPointDate;
        @XmlElement(name = "VATPointDateCode")
        protected String vatPointDateCode;

        /**
         * Gets the value of the vatPointDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getVATPointDate() {
            return vatPointDate;
        }

        /**
         * Sets the value of the vatPointDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setVATPointDate(XMLGregorianCalendar value) {
            this.vatPointDate = value;
        }

        /**
         * Gets the value of the vatPointDateCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVATPointDateCode() {
            return vatPointDateCode;
        }

        /**
         * Sets the value of the vatPointDateCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVATPointDateCode(String value) {
            this.vatPointDateCode = value;
        }

    }

}
