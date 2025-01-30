
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for RejectContentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RejectContentType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.pangaliit.ee/arveldused/e-arve/&gt;LongTextType"&gt;
 *       &lt;attribute name="rejectType" use="required" type="{http://e-arvetekeskus.eu/erp}RejectTypeType" /&gt;
 *       &lt;attribute name="invoiceId" use="required" type="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RejectContentType", propOrder = {
    "value"
})
public class RejectContentType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "rejectType", required = true)
    protected RejectTypeType rejectType;
    @XmlAttribute(name = "invoiceId", required = true)
    protected String invoiceId;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the rejectType property.
     * 
     * @return
     *     possible object is
     *     {@link RejectTypeType }
     *     
     */
    public RejectTypeType getRejectType() {
        return rejectType;
    }

    /**
     * Sets the value of the rejectType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RejectTypeType }
     *     
     */
    public void setRejectType(RejectTypeType value) {
        this.rejectType = value;
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

}
