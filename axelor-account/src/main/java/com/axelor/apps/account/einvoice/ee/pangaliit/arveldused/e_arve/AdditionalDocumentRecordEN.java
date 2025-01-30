
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for AdditionalDocumentRecordEN complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AdditionalDocumentRecordEN">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Type" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="Number" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *         &lt;element name="Date" type="{}DateType" minOccurs="0"/>
 *         &lt;element name="Name" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="URL" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="File" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Filename" type="{}NormalTextType"/>
 *                   &lt;element name="FileMimeCode" type="{}ShortTextType"/>
 *                   &lt;element name="BinaryObject" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
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
@XmlType(name = "AdditionalDocumentRecordEN", propOrder = {
    "type",
    "number",
    "date",
    "name",
    "url",
    "file"
})
public class AdditionalDocumentRecordEN {

    @XmlElement(name = "Type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String type;
    @XmlElement(name = "Number", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String number;
    @XmlElement(name = "Date")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar date;
    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "URL")
    @XmlSchemaType(name = "anyURI")
    protected String url;
    @XmlElement(name = "File")
    protected File file;

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

    /**
     * Gets the value of the number property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNumber(String value) {
        this.number = value;
    }

    /**
     * Gets the value of the date property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the url property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setURL(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the file property.
     *
     * @return
     *     possible object is
     *     {@link File }
     *
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     *
     * @param value
     *     allowed object is
     *     {@link File }
     *
     */
    public void setFile(File value) {
        this.file = value;
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
     *         &lt;element name="Filename" type="{}NormalTextType"/>
     *         &lt;element name="FileMimeCode" type="{}ShortTextType"/>
     *         &lt;element name="BinaryObject" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
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
        "filename",
        "fileMimeCode",
        "binaryObject"
    })
    public static class File {

        @XmlElement(name = "Filename", required = true)
        protected String filename;
        @XmlElement(name = "FileMimeCode", required = true)
        protected String fileMimeCode;
        @XmlElement(name = "BinaryObject")
        protected byte[] binaryObject;

        /**
         * Gets the value of the filename property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Sets the value of the filename property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFilename(String value) {
            this.filename = value;
        }

        /**
         * Gets the value of the fileMimeCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFileMimeCode() {
            return fileMimeCode;
        }

        /**
         * Sets the value of the fileMimeCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFileMimeCode(String value) {
            this.fileMimeCode = value;
        }

        /**
         * Gets the value of the binaryObject property.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getBinaryObject() {
            return binaryObject;
        }

        /**
         * Sets the value of the binaryObject property.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setBinaryObject(byte[] value) {
            this.binaryObject = value;
        }

    }

}
