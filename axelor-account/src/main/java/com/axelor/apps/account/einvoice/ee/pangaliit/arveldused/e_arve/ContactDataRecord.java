
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ContactDataRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContactDataRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ContactName" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="ContactPersonCode" type="{}RegType" minOccurs="0"/>
 *         &lt;element name="PhoneNumber" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="FaxNumber" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="URL" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="E-mailAddress" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value=".+@+.+"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LegalAddress" type="{}AddressRecord" minOccurs="0"/>
 *         &lt;element name="MailAddress" type="{}AddressRecord" minOccurs="0"/>
 *         &lt;element name="ContactInformation" type="{}ExtensionRecord" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContactDataRecord", propOrder = {
    "contactName",
    "contactPersonCode",
    "phoneNumber",
    "faxNumber",
    "url",
    "eMailAddress",
    "legalAddress",
    "mailAddress",
    "contactInformation"
})
public class ContactDataRecord {

    @XmlElement(name = "ContactName")
    protected String contactName;
    @XmlElement(name = "ContactPersonCode")
    protected String contactPersonCode;
    @XmlElement(name = "PhoneNumber")
    protected String phoneNumber;
    @XmlElement(name = "FaxNumber")
    protected String faxNumber;
    @XmlElement(name = "URL")
    protected String url;
    @XmlElement(name = "E-mailAddress")
    protected String eMailAddress;
    @XmlElement(name = "LegalAddress")
    protected AddressRecord legalAddress;
    @XmlElement(name = "MailAddress")
    protected AddressRecord mailAddress;
    @XmlElement(name = "ContactInformation")
    protected List<ExtensionRecord> contactInformation;

    /**
     * Gets the value of the contactName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * Sets the value of the contactName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContactName(String value) {
        this.contactName = value;
    }

    /**
     * Gets the value of the contactPersonCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContactPersonCode() {
        return contactPersonCode;
    }

    /**
     * Sets the value of the contactPersonCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContactPersonCode(String value) {
        this.contactPersonCode = value;
    }

    /**
     * Gets the value of the phoneNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the value of the phoneNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    /**
     * Gets the value of the faxNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaxNumber() {
        return faxNumber;
    }

    /**
     * Sets the value of the faxNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaxNumber(String value) {
        this.faxNumber = value;
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
     * Gets the value of the eMailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEMailAddress() {
        return eMailAddress;
    }

    /**
     * Sets the value of the eMailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEMailAddress(String value) {
        this.eMailAddress = value;
    }

    /**
     * Gets the value of the legalAddress property.
     * 
     * @return
     *     possible object is
     *     {@link AddressRecord }
     *     
     */
    public AddressRecord getLegalAddress() {
        return legalAddress;
    }

    /**
     * Sets the value of the legalAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressRecord }
     *     
     */
    public void setLegalAddress(AddressRecord value) {
        this.legalAddress = value;
    }

    /**
     * Gets the value of the mailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link AddressRecord }
     *     
     */
    public AddressRecord getMailAddress() {
        return mailAddress;
    }

    /**
     * Sets the value of the mailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressRecord }
     *     
     */
    public void setMailAddress(AddressRecord value) {
        this.mailAddress = value;
    }

    /**
     * Gets the value of the contactInformation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contactInformation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContactInformation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExtensionRecord }
     * 
     * 
     */
    public List<ExtensionRecord> getContactInformation() {
        if (contactInformation == null) {
            contactInformation = new ArrayList<ExtensionRecord>();
        }
        return this.contactInformation;
    }

}
