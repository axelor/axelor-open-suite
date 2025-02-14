
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import org.w3c.dom.Element;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for ExtensionRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtensionRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InformationName" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="InformationContent" type="{}LongTextType"/>
 *         &lt;element name="CustomContent" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any processContents='skip'/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="extensionId" type="{}ShortTextType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionRecord", propOrder = {
    "informationName",
    "informationContent",
    "customContent"
})
public class ExtensionRecord {

    @XmlElement(name = "InformationName")
    protected String informationName;
    @XmlElement(name = "InformationContent", required = true)
    protected String informationContent;
    @XmlElement(name = "CustomContent")
    protected CustomContent customContent;
    @XmlAttribute(name = "extensionId")
    protected String extensionId;

    /**
     * Gets the value of the informationName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInformationName() {
        return informationName;
    }

    /**
     * Sets the value of the informationName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInformationName(String value) {
        this.informationName = value;
    }

    /**
     * Gets the value of the informationContent property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInformationContent() {
        return informationContent;
    }

    /**
     * Sets the value of the informationContent property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInformationContent(String value) {
        this.informationContent = value;
    }

    /**
     * Gets the value of the customContent property.
     *
     * @return
     *     possible object is
     *     {@link CustomContent }
     *
     */
    public CustomContent getCustomContent() {
        return customContent;
    }

    /**
     * Sets the value of the customContent property.
     *
     * @param value
     *     allowed object is
     *     {@link CustomContent }
     *
     */
    public void setCustomContent(CustomContent value) {
        this.customContent = value;
    }

    /**
     * Gets the value of the extensionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtensionId() {
        return extensionId;
    }

    /**
     * Sets the value of the extensionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtensionId(String value) {
        this.extensionId = value;
    }


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
     *         &lt;any processContents='skip'/>
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
        "any"
    })
    public static class CustomContent {

        @XmlAnyElement
        protected Element any;

        /**
         * Gets the value of the any property.
         * 
         * @return
         *     possible object is
         *     {@link Element }
         *     
         */
        public Element getAny() {
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
        public void setAny(Element value) {
            this.any = value;
        }

    }

}
