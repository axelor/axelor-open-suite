
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for InvoiceAttachmentRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvoiceAttachmentRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="10"&gt;
 *         &lt;element name="invoiceId" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *       &lt;attribute name="startIndex" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="onlyInvoice" type="{http://www.pangaliit.ee/arveldused/e-arve/}YesNoType" /&gt;
 *       &lt;attribute name="operator" type="{http://www.pangaliit.ee/arveldused/e-arve/}YesNoType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvoiceAttachmentRequestType", propOrder = {
    "invoiceId"
})
public class InvoiceAttachmentRequestType {

    @XmlElement(required = true)
    protected List<String> invoiceId;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;
    @XmlAttribute(name = "startIndex", required = true)
    protected BigInteger startIndex;
    @XmlAttribute(name = "onlyInvoice")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String onlyInvoice;
    @XmlAttribute(name = "operator")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String operator;

    /**
     * Gets the value of the invoiceId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the invoiceId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvoiceId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getInvoiceId() {
        if (invoiceId == null) {
            invoiceId = new ArrayList<String>();
        }
        return this.invoiceId;
    }

    /**
     * Gets the value of the authPhrase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthPhrase() {
        return authPhrase;
    }

    /**
     * Sets the value of the authPhrase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthPhrase(String value) {
        this.authPhrase = value;
    }

    /**
     * Gets the value of the startIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the value of the startIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setStartIndex(BigInteger value) {
        this.startIndex = value;
    }

    /**
     * Gets the value of the onlyInvoice property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnlyInvoice() {
        return onlyInvoice;
    }

    /**
     * Sets the value of the onlyInvoice property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnlyInvoice(String value) {
        this.onlyInvoice = value;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperator(String value) {
        this.operator = value;
    }

}
