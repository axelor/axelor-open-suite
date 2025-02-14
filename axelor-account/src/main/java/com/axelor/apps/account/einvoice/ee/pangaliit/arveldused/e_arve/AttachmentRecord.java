
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import java.math.BigInteger;


/**
 * <p>Java class for AttachmentRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttachmentRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FileName" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="FileBase64" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="FileSize" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttachmentRecord", propOrder = {
    "fileName",
    "fileBase64",
    "fileSize"
})
public class AttachmentRecord {

    @XmlElement(name = "FileName")
    protected String fileName;
    @XmlElement(name = "FileBase64", required = true)
    protected byte[] fileBase64;
    @XmlElement(name = "FileSize")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger fileSize;

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
     * Gets the value of the fileBase64 property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getFileBase64() {
        return fileBase64;
    }

    /**
     * Sets the value of the fileBase64 property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setFileBase64(byte[] value) {
        this.fileBase64 = value;
    }

    /**
     * Gets the value of the fileSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileSize() {
        return fileSize;
    }

    /**
     * Sets the value of the fileSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileSize(BigInteger value) {
        this.fileSize = value;
    }

}
