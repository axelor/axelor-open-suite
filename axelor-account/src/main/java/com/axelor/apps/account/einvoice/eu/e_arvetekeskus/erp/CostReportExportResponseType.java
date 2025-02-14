
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CostReportExportResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CostReportExportResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="includesLatest" use="required" type="{http://e-arvetekeskus.eu/erp}YesNoType" /&gt;
 *       &lt;attribute name="latestChange" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CostReportExportResponseType", propOrder = {
    "any"
})
public class CostReportExportResponseType {

    @XmlAnyElement(lax = true)
    protected Object any;
    @XmlAttribute(name = "includesLatest", required = true)
    protected YesNoType includesLatest;
    @XmlAttribute(name = "latestChange", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar latestChange;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

    /**
     * Gets the value of the includesLatest property.
     * 
     * @return
     *     possible object is
     *     {@link YesNoType }
     *     
     */
    public YesNoType getIncludesLatest() {
        return includesLatest;
    }

    /**
     * Sets the value of the includesLatest property.
     * 
     * @param value
     *     allowed object is
     *     {@link YesNoType }
     *     
     */
    public void setIncludesLatest(YesNoType value) {
        this.includesLatest = value;
    }

    /**
     * Gets the value of the latestChange property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLatestChange() {
        return latestChange;
    }

    /**
     * Sets the value of the latestChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLatestChange(XMLGregorianCalendar value) {
        this.latestChange = value;
    }

}
