
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.*;


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
 *         &lt;element ref="{http://e-arvetekeskus.eu/erp}PdfAttachment"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pdfAttachment"
})
@XmlRootElement(name = "BuyInvoicePdfResponse")
public class BuyInvoicePdfResponse {

    @XmlElement(name = "PdfAttachment", required = true)
    protected Base64FileType pdfAttachment;

    /**
     * Gets the value of the pdfAttachment property.
     * 
     * @return
     *     possible object is
     *     {@link Base64FileType }
     *     
     */
    public Base64FileType getPdfAttachment() {
        return pdfAttachment;
    }

    /**
     * Sets the value of the pdfAttachment property.
     * 
     * @param value
     *     allowed object is
     *     {@link Base64FileType }
     *     
     */
    public void setPdfAttachment(Base64FileType value) {
        this.pdfAttachment = value;
    }

}
