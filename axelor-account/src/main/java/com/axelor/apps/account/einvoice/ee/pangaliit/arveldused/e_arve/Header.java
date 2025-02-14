
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
 *         &lt;element name="Test" type="{}YesNoType" minOccurs="0"/>
 *         &lt;element name="Date" type="{}DateType"/>
 *         &lt;element name="FileId" type="{}ShortTextType"/>
 *         &lt;element name="AppId" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="Version" type="{}ShortTextType"/>
 *         &lt;element name="SenderId" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="ReceiverId" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="ContractId" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="PayeeAccountNumber" type="{}AccountType" minOccurs="0"/>
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
        "test",
        "date",
        "fileId",
        "appId",
        "version",
        "senderId",
        "receiverId",
        "contractId",
        "payeeAccountNumber"
})
@XmlRootElement(name = "Header")
public class Header {

    @XmlElement(name = "Test")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String test;
    @XmlElement(name = "Date", required = true)
    @XmlSchemaType(name = "date")
    protected String date;
    @XmlElement(name = "FileId", required = true)
    protected String fileId;
    @XmlElement(name = "AppId")
    protected String appId;
    @XmlElement(name = "Version", required = true)
    protected String version;
    @XmlElement(name = "SenderId")
    protected String senderId;
    @XmlElement(name = "ReceiverId")
    protected String receiverId;
    @XmlElement(name = "ContractId")
    protected String contractId;
    @XmlElement(name = "PayeeAccountNumber")
    protected String payeeAccountNumber;

    /**
     * Gets the value of the test property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTest(String value) {
        this.test = value;
    }

    /**
     * Gets the value of the date property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDate(LocalDate value) {
        this.date = value.toString();
    }

    /**
     * Gets the value of the fileId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public LocalDate getFileId() {
        return LocalDate.parse(fileId);
    }

    /**
     * Sets the value of the fileId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFileId(String value) {
        this.fileId = value;
    }

    /**
     * Gets the value of the appId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the value of the appId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAppId(String value) {
        this.appId = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the senderId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Sets the value of the senderId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSenderId(String value) {
        this.senderId = value;
    }

    /**
     * Gets the value of the receiverId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReceiverId() {
        return receiverId;
    }

    /**
     * Sets the value of the receiverId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReceiverId(String value) {
        this.receiverId = value;
    }

    /**
     * Gets the value of the contractId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContractId() {
        return contractId;
    }

    /**
     * Sets the value of the contractId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContractId(String value) {
        this.contractId = value;
    }

    /**
     * Gets the value of the payeeAccountNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPayeeAccountNumber() {
        return payeeAccountNumber;
    }

    /**
     * Sets the value of the payeeAccountNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPayeeAccountNumber(String value) {
        this.payeeAccountNumber = value;
    }

}
