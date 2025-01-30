
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for SellerPartyRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SellerPartyRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GLN" type="{}GLNType" minOccurs="0"/>
 *         &lt;element name="TransactionPartnerCode" type="{}PartnerCodeType" minOccurs="0"/>
 *         &lt;element name="UniqueCode" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="Name" type="{}NormalTextType"/>
 *         &lt;element name="DepId" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="RegNumber" type="{}RegType"/>
 *         &lt;element name="VATRegNumber" type="{}RegType" minOccurs="0"/>
 *         &lt;element name="ContactData" type="{}ContactDataRecord" minOccurs="0"/>
 *         &lt;element name="AccountInfo" type="{}AccountDataRecord" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "SellerPartyRecord", propOrder = {
    "gln",
    "transactionPartnerCode",
    "uniqueCode",
    "name",
    "depId",
    "regNumber",
    "vatRegNumber",
    "contactData",
    "accountInfo",
    "extension"
})
public class SellerPartyRecord {

    @XmlElement(name = "GLN")
    protected String gln;
    @XmlElement(name = "TransactionPartnerCode")
    protected String transactionPartnerCode;
    @XmlElement(name = "UniqueCode")
    protected String uniqueCode;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "DepId")
    protected String depId;
    @XmlElement(name = "RegNumber", required = true)
    protected String regNumber;
    @XmlElement(name = "VATRegNumber")
    protected String vatRegNumber;
    @XmlElement(name = "ContactData")
    protected ContactDataRecord contactData;
    @XmlElement(name = "AccountInfo")
    protected List<AccountDataRecord> accountInfo;
    @XmlElement(name = "Extension")
    protected List<ExtensionRecord> extension;

    /**
     * Gets the value of the gln property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGLN() {
        return gln;
    }

    /**
     * Sets the value of the gln property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGLN(String value) {
        this.gln = value;
    }

    /**
     * Gets the value of the transactionPartnerCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionPartnerCode() {
        return transactionPartnerCode;
    }

    /**
     * Sets the value of the transactionPartnerCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionPartnerCode(String value) {
        this.transactionPartnerCode = value;
    }

    /**
     * Gets the value of the uniqueCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueCode() {
        return uniqueCode;
    }

    /**
     * Sets the value of the uniqueCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueCode(String value) {
        this.uniqueCode = value;
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
     * Gets the value of the depId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepId() {
        return depId;
    }

    /**
     * Sets the value of the depId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepId(String value) {
        this.depId = value;
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
     * Gets the value of the vatRegNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVATRegNumber() {
        return vatRegNumber;
    }

    /**
     * Sets the value of the vatRegNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVATRegNumber(String value) {
        this.vatRegNumber = value;
    }

    /**
     * Gets the value of the contactData property.
     * 
     * @return
     *     possible object is
     *     {@link ContactDataRecord }
     *     
     */
    public ContactDataRecord getContactData() {
        return contactData;
    }

    /**
     * Sets the value of the contactData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactDataRecord }
     *     
     */
    public void setContactData(ContactDataRecord value) {
        this.contactData = value;
    }

    /**
     * Gets the value of the accountInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accountInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccountInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccountDataRecord }
     * 
     * 
     */
    public List<AccountDataRecord> getAccountInfo() {
        if (accountInfo == null) {
            accountInfo = new ArrayList<AccountDataRecord>();
        }
        return this.accountInfo;
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

}
