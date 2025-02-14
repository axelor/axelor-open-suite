
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ItemEntryENType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ItemEntryENType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InfoText" type="{}LongTextType" minOccurs="0"/>
 *         &lt;element name="ItemDetailedDescription" type="{}LongTextType" minOccurs="0"/>
 *         &lt;element name="ItemAdditionalId" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
 *                 &lt;attribute name="schemeId" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ObjectId" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>normalizedString">
 *                 &lt;attribute name="schemeId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ItemClassification" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;>LongTextType">
 *                 &lt;attribute name="schemeId" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *                 &lt;attribute name="schemeVersionId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ItemCountryOfOrigin" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *         &lt;element name="Period" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="StartDate" type="{}DateType" minOccurs="0"/>
 *                   &lt;element name="EndDate" type="{}DateType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Addition" type="{}AdditionENType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ItemPriceInfo">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ItemDiscountSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *                   &lt;element name="ItemPriceBaseQuantity" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
 *                   &lt;element name="ItemPriceBaseQuantityUnit" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
 *                   &lt;element name="ItemGrossPrice" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PurchaseOrderLineRef" type="{}LongTextType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemEntryENType", propOrder = {
    "infoText",
    "itemDetailedDescription",
    "itemAdditionalId",
    "objectId",
    "itemClassification",
    "itemCountryOfOrigin",
    "period",
    "addition",
    "itemPriceInfo",
    "purchaseOrderLineRef"
})
public class ItemEntryENType {

    @XmlElement(name = "InfoText")
    protected String infoText;
    @XmlElement(name = "ItemDetailedDescription")
    protected String itemDetailedDescription;
    @XmlElement(name = "ItemAdditionalId")
    protected ItemAdditionalId itemAdditionalId;
    @XmlElement(name = "ObjectId")
    protected ObjectId objectId;
    @XmlElement(name = "ItemClassification")
    protected List<ItemClassification> itemClassification;
    @XmlElement(name = "ItemCountryOfOrigin")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String itemCountryOfOrigin;
    @XmlElement(name = "Period")
    protected Period period;
    @XmlElement(name = "Addition")
    protected List<AdditionENType> addition;
    @XmlElement(name = "ItemPriceInfo", required = true)
    protected ItemPriceInfo itemPriceInfo;
    @XmlElement(name = "PurchaseOrderLineRef")
    protected String purchaseOrderLineRef;

    /**
     * Gets the value of the infoText property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInfoText() {
        return infoText;
    }

    /**
     * Sets the value of the infoText property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInfoText(String value) {
        this.infoText = value;
    }

    /**
     * Gets the value of the itemDetailedDescription property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getItemDetailedDescription() {
        return itemDetailedDescription;
    }

    /**
     * Sets the value of the itemDetailedDescription property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setItemDetailedDescription(String value) {
        this.itemDetailedDescription = value;
    }

    /**
     * Gets the value of the itemAdditionalId property.
     *
     * @return
     *     possible object is
     *     {@link ItemAdditionalId }
     *
     */
    public ItemAdditionalId getItemAdditionalId() {
        return itemAdditionalId;
    }

    /**
     * Sets the value of the itemAdditionalId property.
     *
     * @param value
     *     allowed object is
     *     {@link ItemAdditionalId }
     *
     */
    public void setItemAdditionalId(ItemAdditionalId value) {
        this.itemAdditionalId = value;
    }

    /**
     * Gets the value of the objectId property.
     *
     * @return
     *     possible object is
     *     {@link ObjectId }
     *
     */
    public ObjectId getObjectId() {
        return objectId;
    }

    /**
     * Sets the value of the objectId property.
     *
     * @param value
     *     allowed object is
     *     {@link ObjectId }
     *
     */
    public void setObjectId(ObjectId value) {
        this.objectId = value;
    }

    /**
     * Gets the value of the itemClassification property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the itemClassification property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItemClassification().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ItemClassification }
     *
     *
     */
    public List<ItemClassification> getItemClassification() {
        if (itemClassification == null) {
            itemClassification = new ArrayList<ItemClassification>();
        }
        return this.itemClassification;
    }

    /**
     * Gets the value of the itemCountryOfOrigin property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getItemCountryOfOrigin() {
        return itemCountryOfOrigin;
    }

    /**
     * Sets the value of the itemCountryOfOrigin property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setItemCountryOfOrigin(String value) {
        this.itemCountryOfOrigin = value;
    }

    /**
     * Gets the value of the period property.
     *
     * @return
     *     possible object is
     *     {@link Period }
     *
     */
    public Period getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     *
     * @param value
     *     allowed object is
     *     {@link Period }
     *
     */
    public void setPeriod(Period value) {
        this.period = value;
    }

    /**
     * Gets the value of the addition property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addition property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddition().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionENType }
     *
     *
     */
    public List<AdditionENType> getAddition() {
        if (addition == null) {
            addition = new ArrayList<AdditionENType>();
        }
        return this.addition;
    }

    /**
     * Gets the value of the itemPriceInfo property.
     *
     * @return
     *     possible object is
     *     {@link ItemPriceInfo }
     *
     */
    public ItemPriceInfo getItemPriceInfo() {
        return itemPriceInfo;
    }

    /**
     * Sets the value of the itemPriceInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link ItemPriceInfo }
     *
     */
    public void setItemPriceInfo(ItemPriceInfo value) {
        this.itemPriceInfo = value;
    }

    /**
     * Gets the value of the purchaseOrderLineRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPurchaseOrderLineRef() {
        return purchaseOrderLineRef;
    }

    /**
     * Sets the value of the purchaseOrderLineRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPurchaseOrderLineRef(String value) {
        this.purchaseOrderLineRef = value;
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
    public static class ItemAdditionalId {

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
     *     &lt;extension base="&lt;>LongTextType">
     *       &lt;attribute name="schemeId" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
     *       &lt;attribute name="schemeVersionId" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
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
    public static class ItemClassification {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "schemeId", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String schemeId;
        @XmlAttribute(name = "schemeVersionId")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String schemeVersionId;

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

        /**
         * Gets the value of the schemeVersionId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSchemeVersionId() {
            return schemeVersionId;
        }

        /**
         * Sets the value of the schemeVersionId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSchemeVersionId(String value) {
            this.schemeVersionId = value;
        }

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
     *         &lt;element name="ItemDiscountSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
     *         &lt;element name="ItemPriceBaseQuantity" type="{}Decimal2FractionDigitsType" minOccurs="0"/>
     *         &lt;element name="ItemPriceBaseQuantityUnit" type="{http://www.w3.org/2001/XMLSchema}normalizedString" minOccurs="0"/>
     *         &lt;element name="ItemGrossPrice" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
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
        "itemDiscountSum",
        "itemPriceBaseQuantity",
        "itemPriceBaseQuantityUnit",
        "itemGrossPrice"
    })
    public static class ItemPriceInfo {

        @XmlElement(name = "ItemDiscountSum")
        protected BigDecimal itemDiscountSum;
        @XmlElement(name = "ItemPriceBaseQuantity")
        protected BigDecimal itemPriceBaseQuantity;
        @XmlElement(name = "ItemPriceBaseQuantityUnit")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String itemPriceBaseQuantityUnit;
        @XmlElement(name = "ItemGrossPrice")
        protected BigDecimal itemGrossPrice;

        /**
         * Gets the value of the itemDiscountSum property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getItemDiscountSum() {
            return itemDiscountSum;
        }

        /**
         * Sets the value of the itemDiscountSum property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setItemDiscountSum(BigDecimal value) {
            this.itemDiscountSum = value;
        }

        /**
         * Gets the value of the itemPriceBaseQuantity property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getItemPriceBaseQuantity() {
            return itemPriceBaseQuantity;
        }

        /**
         * Sets the value of the itemPriceBaseQuantity property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setItemPriceBaseQuantity(BigDecimal value) {
            this.itemPriceBaseQuantity = value;
        }

        /**
         * Gets the value of the itemPriceBaseQuantityUnit property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getItemPriceBaseQuantityUnit() {
            return itemPriceBaseQuantityUnit;
        }

        /**
         * Sets the value of the itemPriceBaseQuantityUnit property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setItemPriceBaseQuantityUnit(String value) {
            this.itemPriceBaseQuantityUnit = value;
        }

        /**
         * Gets the value of the itemGrossPrice property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getItemGrossPrice() {
            return itemGrossPrice;
        }

        /**
         * Sets the value of the itemGrossPrice property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setItemGrossPrice(BigDecimal value) {
            this.itemGrossPrice = value;
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
    public static class ObjectId {

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
     *         &lt;element name="StartDate" type="{}DateType" minOccurs="0"/>
     *         &lt;element name="EndDate" type="{}DateType" minOccurs="0"/>
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
        "startDate",
        "endDate"
    })
    public static class Period {

        @XmlElement(name = "StartDate")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar startDate;
        @XmlElement(name = "EndDate")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar endDate;

        /**
         * Gets the value of the startDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getStartDate() {
            return startDate;
        }

        /**
         * Sets the value of the startDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setStartDate(XMLGregorianCalendar value) {
            this.startDate = value;
        }

        /**
         * Gets the value of the endDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEndDate() {
            return endDate;
        }

        /**
         * Sets the value of the endDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEndDate(XMLGregorianCalendar value) {
            this.endDate = value;
        }

    }

}
