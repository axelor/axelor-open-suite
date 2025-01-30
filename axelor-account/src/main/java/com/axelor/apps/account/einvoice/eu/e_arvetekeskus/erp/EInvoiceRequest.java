
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;


import com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve.EInvoice;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.pangaliit.ee/arveldused/e-arve/}E_Invoice"/&gt;
 *         &lt;element ref="{http://e-arvetekeskus.eu/erp}PdfAttachment" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *       &lt;attribute name="operator" type="{http://www.pangaliit.ee/arveldused/e-arve/}YesNoType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "eInvoice",
    "pdfAttachment"
})
@XmlRootElement(name = "EInvoiceRequest")
public class EInvoiceRequest {

    @XmlElement(name = "E_Invoice", namespace = "http://www.pangaliit.ee/arveldused/e-arve/", required = true)
    protected EInvoice eInvoice;
    @XmlElement(name = "PdfAttachment")
    protected List<Base64FileType> pdfAttachment;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;
    @XmlAttribute(name = "operator")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String operator;

    /**
     * Estonian e-invoice version 1.0. More info available at http://www.pangaliit.ee/arveldused/e-arve/
     * 
     * @return
     *     possible object is
     *     {@link EInvoice }
     *     
     */
    public EInvoice getEInvoice() {
        return eInvoice;
    }

    /**
     * Sets the value of the eInvoice property.
     * 
     * @param value
     *     allowed object is
     *     {@link EInvoice }
     *     
     */
    public void setEInvoice(EInvoice value) {
        this.eInvoice = value;
    }

    /**
     * Gets the value of the pdfAttachment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pdfAttachment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPdfAttachment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Base64FileType }
     * 
     * 
     */
    public List<Base64FileType> getPdfAttachment() {
        if (pdfAttachment == null) {
            pdfAttachment = new ArrayList<Base64FileType>();
        }
        return this.pdfAttachment;
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
