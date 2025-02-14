
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
 *         &lt;element ref="{}InvoiceParties"/>
 *         &lt;element ref="{}InvoiceInformation"/>
 *         &lt;element ref="{}InvoiceSumGroup" maxOccurs="2"/>
 *         &lt;element ref="{}InvoiceItem"/>
 *         &lt;element ref="{}AdditionalInformation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}AttachmentFile" minOccurs="0"/>
 *         &lt;element ref="{}PaymentInfo"/>
 *       &lt;/sequence>
 *       &lt;attribute name="invoiceId" use="required" type="{}NormalTextType" />
 *       &lt;attribute name="serviceId" type="{}ShortTextType" />
 *       &lt;attribute name="regNumber" use="required" type="{}RegType" />
 *       &lt;attribute name="channelId" type="{}EncodingType" />
 *       &lt;attribute name="channelAddress" type="{}NormalTextType" />
 *       &lt;attribute name="factoring" type="{}YesNoType" />
 *       &lt;attribute name="templateId" type="{}NormalTextType" />
 *       &lt;attribute name="languageId" type="{}LanguageType" />
 *       &lt;attribute name="presentment" type="{}YesNoType" />
 *       &lt;attribute name="invoiceGlobUniqId" type="{}NormalTextType" />
 *       &lt;attribute name="sellerContractId" type="{}NormalTextType" />
 *       &lt;attribute name="sellerRegnumber" use="required" type="{}RegType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "invoiceParties",
    "invoiceInformation",
    "invoiceSumGroup",
    "invoiceItem",
    "additionalInformation",
    "attachmentFile",
    "paymentInfo"
})
@XmlRootElement(name = "Invoice")
public class Invoice {

    @XmlElement(name = "InvoiceParties", required = true)
    protected InvoiceParties invoiceParties;
    @XmlElement(name = "InvoiceInformation", required = true)
    protected InvoiceInformation invoiceInformation;
    @XmlElement(name = "InvoiceSumGroup", required = true)
    protected List<InvoiceSumGroup> invoiceSumGroup;
    @XmlElement(name = "InvoiceItem", required = true)
    protected InvoiceItem invoiceItem;
    @XmlElement(name = "AdditionalInformation")
    protected List<ExtensionRecord> additionalInformation;
    @XmlElement(name = "AttachmentFile")
    protected AttachmentRecord attachmentFile;
    @XmlElement(name = "PaymentInfo", required = true)
    protected PaymentInfo paymentInfo;
    @XmlAttribute(name = "invoiceId", required = true)
    protected String invoiceId;
    @XmlAttribute(name = "serviceId")
    protected String serviceId;
    @XmlAttribute(name = "regNumber", required = true)
    protected String regNumber;
    @XmlAttribute(name = "channelId")
    protected String channelId;
    @XmlAttribute(name = "channelAddress")
    protected String channelAddress;
    @XmlAttribute(name = "factoring")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String factoring;
    @XmlAttribute(name = "templateId")
    protected String templateId;
    @XmlAttribute(name = "languageId")
    protected String languageId;
    @XmlAttribute(name = "presentment")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String presentment;
    @XmlAttribute(name = "invoiceGlobUniqId")
    protected String invoiceGlobUniqId;
    @XmlAttribute(name = "sellerContractId")
    protected String sellerContractId;
    @XmlAttribute(name = "sellerRegnumber", required = true)
    protected String sellerRegnumber;

    /**
     * Gets the value of the invoiceParties property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceParties }
     *     
     */
    public InvoiceParties getInvoiceParties() {
        return invoiceParties;
    }

    /**
     * Sets the value of the invoiceParties property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceParties }
     *     
     */
    public void setInvoiceParties(InvoiceParties value) {
        this.invoiceParties = value;
    }

    /**
     * Gets the value of the invoiceInformation property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceInformation }
     *     
     */
    public InvoiceInformation getInvoiceInformation() {
        return invoiceInformation;
    }

    /**
     * Sets the value of the invoiceInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceInformation }
     *     
     */
    public void setInvoiceInformation(InvoiceInformation value) {
        this.invoiceInformation = value;
    }

    /**
     * Gets the value of the invoiceSumGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the invoiceSumGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvoiceSumGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InvoiceSumGroup }
     * 
     * 
     */
    public List<InvoiceSumGroup> getInvoiceSumGroup() {
        if (invoiceSumGroup == null) {
            invoiceSumGroup = new ArrayList<InvoiceSumGroup>();
        }
        return this.invoiceSumGroup;
    }

    /**
     * Gets the value of the invoiceItem property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceItem }
     *     
     */
    public InvoiceItem getInvoiceItem() {
        return invoiceItem;
    }

    /**
     * Sets the value of the invoiceItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceItem }
     *     
     */
    public void setInvoiceItem(InvoiceItem value) {
        this.invoiceItem = value;
    }

    /**
     * Gets the value of the additionalInformation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additionalInformation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditionalInformation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExtensionRecord }
     * 
     * 
     */
    public List<ExtensionRecord> getAdditionalInformation() {
        if (additionalInformation == null) {
            additionalInformation = new ArrayList<ExtensionRecord>();
        }
        return this.additionalInformation;
    }

    /**
     * Gets the value of the attachmentFile property.
     * 
     * @return
     *     possible object is
     *     {@link AttachmentRecord }
     *     
     */
    public AttachmentRecord getAttachmentFile() {
        return attachmentFile;
    }

    /**
     * Sets the value of the attachmentFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttachmentRecord }
     *     
     */
    public void setAttachmentFile(AttachmentRecord value) {
        this.attachmentFile = value;
    }

    /**
     * Gets the value of the paymentInfo property.
     * 
     * @return
     *     possible object is
     *     {@link PaymentInfo }
     *     
     */
    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    /**
     * Sets the value of the paymentInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentInfo }
     *     
     */
    public void setPaymentInfo(PaymentInfo value) {
        this.paymentInfo = value;
    }

    /**
     * Gets the value of the invoiceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvoiceId() {
        return invoiceId;
    }

    /**
     * Sets the value of the invoiceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvoiceId(String value) {
        this.invoiceId = value;
    }

    /**
     * Gets the value of the serviceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the value of the serviceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceId(String value) {
        this.serviceId = value;
    }

    /**
     * Gets the value of the regNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegNumber() {
        return regNumber;
    }

    /**
     * Sets the value of the regNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegNumber(String value) {
        this.regNumber = value;
    }

    /**
     * Gets the value of the channelId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the value of the channelId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannelId(String value) {
        this.channelId = value;
    }

    /**
     * Gets the value of the channelAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannelAddress() {
        return channelAddress;
    }

    /**
     * Sets the value of the channelAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannelAddress(String value) {
        this.channelAddress = value;
    }

    /**
     * Gets the value of the factoring property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFactoring() {
        return factoring;
    }

    /**
     * Sets the value of the factoring property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFactoring(String value) {
        this.factoring = value;
    }

    /**
     * Gets the value of the templateId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Sets the value of the templateId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateId(String value) {
        this.templateId = value;
    }

    /**
     * Gets the value of the languageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguageId() {
        return languageId;
    }

    /**
     * Sets the value of the languageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguageId(String value) {
        this.languageId = value;
    }

    /**
     * Gets the value of the presentment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPresentment() {
        return presentment;
    }

    /**
     * Sets the value of the presentment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPresentment(String value) {
        this.presentment = value;
    }

    /**
     * Gets the value of the invoiceGlobUniqId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvoiceGlobUniqId() {
        return invoiceGlobUniqId;
    }

    /**
     * Sets the value of the invoiceGlobUniqId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvoiceGlobUniqId(String value) {
        this.invoiceGlobUniqId = value;
    }

    /**
     * Gets the value of the sellerContractId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSellerContractId() {
        return sellerContractId;
    }

    /**
     * Sets the value of the sellerContractId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSellerContractId(String value) {
        this.sellerContractId = value;
    }

    /**
     * Gets the value of the sellerRegnumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSellerRegnumber() {
        return sellerRegnumber;
    }

    /**
     * Sets the value of the sellerRegnumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSellerRegnumber(String value) {
        this.sellerRegnumber = value;
    }

}
