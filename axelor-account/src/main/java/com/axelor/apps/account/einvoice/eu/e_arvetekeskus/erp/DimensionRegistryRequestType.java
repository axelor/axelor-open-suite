
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import com.axelor.apps.account.einvoice.ElementAdapter;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for DimensionRegistryRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DimensionRegistryRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any processContents='skip'/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="format" use="required" type="{http://e-arvetekeskus.eu/erp}RegistryFormatType" /&gt;
 *       &lt;attribute name="replace" type="{http://www.pangaliit.ee/arveldused/e-arve/}YesNoType" /&gt;
 *       &lt;attribute name="parse" type="{http://www.pangaliit.ee/arveldused/e-arve/}YesNoType" /&gt;
 *       &lt;attribute name="parseConnections" type="{http://www.pangaliit.ee/arveldused/e-arve/}YesNoType" /&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DimensionRegistryRequestType", propOrder = {
    "any"
})
public class DimensionRegistryRequestType {

    @XmlJavaTypeAdapter(ElementAdapter.class) // Apply the adapter here
    protected String any;
    @XmlAttribute(name = "format", required = true)
    protected RegistryFormatType format;
    @XmlAttribute(name = "replace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String replace;
    @XmlAttribute(name = "parse")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String parse;
    @XmlAttribute(name = "parseConnections")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String parseConnections;
    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Element }
     *     
     */
    public String getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Element }
     *     
     */
    public void setAny(String value) {
        this.any = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link RegistryFormatType }
     *     
     */
    public RegistryFormatType getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistryFormatType }
     *     
     */
    public void setFormat(RegistryFormatType value) {
        this.format = value;
    }

    /**
     * Gets the value of the replace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplace() {
        return replace;
    }

    /**
     * Sets the value of the replace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplace(String value) {
        this.replace = value;
    }

    /**
     * Gets the value of the parse property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParse() {
        return parse;
    }

    /**
     * Sets the value of the parse property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParse(String value) {
        this.parse = value;
    }

    /**
     * Gets the value of the parseConnections property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParseConnections() {
        return parseConnections;
    }

    /**
     * Sets the value of the parseConnections property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParseConnections(String value) {
        this.parseConnections = value;
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

}
