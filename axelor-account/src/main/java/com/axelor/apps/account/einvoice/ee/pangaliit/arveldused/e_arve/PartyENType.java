
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for PartyENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PartyENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TradingName" type="{}LongTextType" minOccurs="0"/>
 *         &lt;element name="AdditionalLegalInfo" type="{}LongTextType" minOccurs="0"/>
 *         &lt;element name="PartyId" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
 *                 &lt;attribute name="schemeId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PartyElectronicAddress" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
 *                 &lt;attribute name="schemeId" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="TaxRegId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartyENType", propOrder = {
    "tradingName",
    "additionalLegalInfo",
    "partyId",
    "partyElectronicAddress",
    "taxRegId"
})
public class PartyENType {

    @XmlElement(name = "TradingName")
    protected String tradingName;
    @XmlElement(name = "AdditionalLegalInfo")
    protected String additionalLegalInfo;
    @XmlElement(name = "PartyId")
    protected List<PartyId> partyId;
    @XmlElement(name = "PartyElectronicAddress")
    protected PartyElectronicAddress partyElectronicAddress;
    @XmlElement(name = "TaxRegId")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String taxRegId;

    /**
     * Gets the value of the tradingName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTradingName() {
        return tradingName;
    }

    /**
     * Sets the value of the tradingName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTradingName(String value) {
        this.tradingName = value;
    }

    /**
     * Gets the value of the additionalLegalInfo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAdditionalLegalInfo() {
        return additionalLegalInfo;
    }

    /**
     * Sets the value of the additionalLegalInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAdditionalLegalInfo(String value) {
        this.additionalLegalInfo = value;
    }

    /**
     * Gets the value of the partyId property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partyId property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartyId().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartyId }
     *
     *
     */
    public List<PartyId> getPartyId() {
        if (partyId == null) {
            partyId = new ArrayList<PartyId>();
        }
        return this.partyId;
    }

    /**
     * Gets the value of the partyElectronicAddress property.
     *
     * @return
     *     possible object is
     *     {@link PartyElectronicAddress }
     *
     */
    public PartyElectronicAddress getPartyElectronicAddress() {
        return partyElectronicAddress;
    }

    /**
     * Sets the value of the partyElectronicAddress property.
     *
     * @param value
     *     allowed object is
     *     {@link PartyElectronicAddress }
     *
     */
    public void setPartyElectronicAddress(PartyElectronicAddress value) {
        this.partyElectronicAddress = value;
    }

    /**
     * Gets the value of the taxRegId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxRegId() {
        return taxRegId;
    }

    /**
     * Sets the value of the taxRegId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxRegId(String value) {
        this.taxRegId = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
     *       &lt;attribute name="schemeId" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class PartyElectronicAddress {

        @XmlValue
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String value;
        @XmlAttribute(name = "schemeId", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String schemeId;

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
         * Gets the value of the schemeId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSchemeId() {
            return schemeId;
        }

        /**
         * Sets the value of the schemeId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSchemeId(String value) {
            this.schemeId = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
     *       &lt;attribute name="schemeId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class PartyId {

        @XmlValue
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String value;
        @XmlAttribute(name = "schemeId")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String schemeId;

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
         * Gets the value of the schemeId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSchemeId() {
            return schemeId;
        }

        /**
         * Sets the value of the schemeId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSchemeId(String value) {
            this.schemeId = value;
        }

    }

}
