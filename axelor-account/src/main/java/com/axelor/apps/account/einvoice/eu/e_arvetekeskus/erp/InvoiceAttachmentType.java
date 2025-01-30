
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import java.math.BigInteger;


/**
 * <p>Java class for InvoiceAttachmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvoiceAttachmentType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AttachmentContent" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="invoiceId" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType" /&gt;
 *       &lt;attribute name="fileType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="length" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvoiceAttachmentType", propOrder = {
    "attachmentContent"
})
public class InvoiceAttachmentType {

    @XmlElement(name = "AttachmentContent", required = true)
    protected byte[] attachmentContent;
    @XmlAttribute(name = "invoiceId")
    protected String invoiceId;
    @XmlAttribute(name = "fileType")
    protected String fileType;
    @XmlAttribute(name = "fileName")
    protected String fileName;
    @XmlAttribute(name = "length")
    protected BigInteger length;

    /**
     * Gets the value of the attachmentContent property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getAttachmentContent() {
        return attachmentContent;
    }

    /**
     * Sets the value of the attachmentContent property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setAttachmentContent(byte[] value) {
        this.attachmentContent = value;
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
     * Gets the value of the fileType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Sets the value of the fileType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileType(String value) {
        this.fileType = value;
    }

    /**
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLength(BigInteger value) {
        this.length = value;
    }

}
