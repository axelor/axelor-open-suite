
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for CostReportAttachmentResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CostReportAttachmentResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="10" minOccurs="0"&gt;
 *         &lt;element name="Attachment" type="{http://e-arvetekeskus.eu/erp}CostReportAttachmentType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CostReportAttachmentResponseType", propOrder = {
    "attachment"
})
public class CostReportAttachmentResponseType {

    @XmlElement(name = "Attachment")
    protected List<CostReportAttachmentType> attachment;

    /**
     * Gets the value of the attachment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attachment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttachment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CostReportAttachmentType }
     * 
     * 
     */
    public List<CostReportAttachmentType> getAttachment() {
        if (attachment == null) {
            attachment = new ArrayList<CostReportAttachmentType>();
        }
        return this.attachment;
    }

}
