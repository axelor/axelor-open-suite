
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for DimensionRegistryConnectionErrorsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DimensionRegistryConnectionErrorsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ImportError" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="ConnectionDimFrom" type="{http://e-arvetekeskus.eu/erp}DimensionRegistryConnectionPartType"/&gt;
 *                   &lt;element name="ConnectionDimTo" type="{http://e-arvetekeskus.eu/erp}DimensionRegistryConnectionPartType"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DimensionRegistryConnectionErrorsType", propOrder = {
    "importError"
})
public class DimensionRegistryConnectionErrorsType {

    @XmlElement(name = "ImportError", required = true)
    protected List<ImportError> importError;

    /**
     * Gets the value of the importError property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the importError property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImportError().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ImportError }
     *
     *
     */
    public List<ImportError> getImportError() {
        if (importError == null) {
            importError = new ArrayList<ImportError>();
        }
        return this.importError;
    }


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
     *         &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="ConnectionDimFrom" type="{http://e-arvetekeskus.eu/erp}DimensionRegistryConnectionPartType"/&gt;
     *         &lt;element name="ConnectionDimTo" type="{http://e-arvetekeskus.eu/erp}DimensionRegistryConnectionPartType"/&gt;
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
        "code",
        "message",
        "connectionDimFrom",
        "connectionDimTo"
    })
    public static class ImportError {

        @XmlElement(name = "Code", required = true)
        protected String code;
        @XmlElement(name = "Message", required = true)
        protected String message;
        @XmlElement(name = "ConnectionDimFrom", required = true)
        protected DimensionRegistryConnectionPartType connectionDimFrom;
        @XmlElement(name = "ConnectionDimTo", required = true)
        protected DimensionRegistryConnectionPartType connectionDimTo;

        /**
         * Gets the value of the code property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCode() {
            return code;
        }

        /**
         * Sets the value of the code property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCode(String value) {
            this.code = value;
        }

        /**
         * Gets the value of the message property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMessage() {
            return message;
        }

        /**
         * Sets the value of the message property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMessage(String value) {
            this.message = value;
        }

        /**
         * Gets the value of the connectionDimFrom property.
         * 
         * @return
         *     possible object is
         *     {@link DimensionRegistryConnectionPartType }
         *     
         */
        public DimensionRegistryConnectionPartType getConnectionDimFrom() {
            return connectionDimFrom;
        }

        /**
         * Sets the value of the connectionDimFrom property.
         * 
         * @param value
         *     allowed object is
         *     {@link DimensionRegistryConnectionPartType }
         *     
         */
        public void setConnectionDimFrom(DimensionRegistryConnectionPartType value) {
            this.connectionDimFrom = value;
        }

        /**
         * Gets the value of the connectionDimTo property.
         * 
         * @return
         *     possible object is
         *     {@link DimensionRegistryConnectionPartType }
         *     
         */
        public DimensionRegistryConnectionPartType getConnectionDimTo() {
            return connectionDimTo;
        }

        /**
         * Sets the value of the connectionDimTo property.
         * 
         * @param value
         *     allowed object is
         *     {@link DimensionRegistryConnectionPartType }
         *     
         */
        public void setConnectionDimTo(DimensionRegistryConnectionPartType value) {
            this.connectionDimTo = value;
        }

    }

}
