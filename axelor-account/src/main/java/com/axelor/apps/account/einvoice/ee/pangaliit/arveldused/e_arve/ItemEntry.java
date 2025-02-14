
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element name="RowNo" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="SerialNumber" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="SellerProductId" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="BuyerProductId" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="TaricCode" type="{}EncodingType" minOccurs="0"/>
 *         &lt;element name="Accounting" type="{}AccountingRecord" minOccurs="0"/>
 *         &lt;element name="CustomerRef" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="Description" type="{}LongTextSingleType"/>
 *         &lt;element name="EAN" type="{}NormalTextType" minOccurs="0"/>
 *         &lt;element name="InitialReading" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="FinalReading" type="{}ShortTextType" minOccurs="0"/>
 *         &lt;element name="ItemReserve" type="{}ExtensionRecord" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ItemDetailInfo" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ItemUnit" type="{}ShortTextType" minOccurs="0"/>
 *                   &lt;element name="ItemAmount" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *                   &lt;element name="ItemPrice" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ItemSum" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
 *         &lt;element name="Addition" type="{}AdditionRecord" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="VAT" type="{}VATRecord" minOccurs="0"/>
 *         &lt;element name="ItemTotal" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
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
    "rowNo",
    "serialNumber",
    "sellerProductId",
    "buyerProductId",
    "taricCode",
    "accounting",
    "customerRef",
    "description",
    "ean",
    "initialReading",
    "finalReading",
    "itemReserve",
    "itemDetailInfo",
    "itemSum",
    "addition",
    "vat",
    "itemTotal"
})
@XmlRootElement(name = "ItemEntry")
public class ItemEntry {

    @XmlElement(name = "RowNo")
    protected String rowNo;
    @XmlElement(name = "SerialNumber")
    protected String serialNumber;
    @XmlElement(name = "SellerProductId")
    protected String sellerProductId;
    @XmlElement(name = "BuyerProductId")
    protected String buyerProductId;
    @XmlElement(name = "TaricCode")
    protected String taricCode;
    @XmlElement(name = "Accounting")
    protected AccountingRecord accounting;
    @XmlElement(name = "CustomerRef")
    protected String customerRef;
    @XmlElement(name = "Description", required = true)
    protected String description;
    @XmlElement(name = "EAN")
    protected String ean;
    @XmlElement(name = "InitialReading")
    protected String initialReading;
    @XmlElement(name = "FinalReading")
    protected String finalReading;
    @XmlElement(name = "ItemReserve")
    protected List<ExtensionRecord> itemReserve;
    @XmlElement(name = "ItemDetailInfo")
    protected List<ItemDetailInfo> itemDetailInfo;
    @XmlElement(name = "ItemSum")
    protected BigDecimal itemSum;
    @XmlElement(name = "Addition")
    protected List<AdditionRecord> addition;
    @XmlElement(name = "VAT")
    protected VATRecord vat;
    @XmlElement(name = "ItemTotal")
    protected BigDecimal itemTotal;

    /**
     * Gets the value of the rowNo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRowNo() {
        return rowNo;
    }

    /**
     * Sets the value of the rowNo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRowNo(String value) {
        this.rowNo = value;
    }

    /**
     * Gets the value of the serialNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the value of the serialNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSerialNumber(String value) {
        this.serialNumber = value;
    }

    /**
     * Gets the value of the sellerProductId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSellerProductId() {
        return sellerProductId;
    }

    /**
     * Sets the value of the sellerProductId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSellerProductId(String value) {
        this.sellerProductId = value;
    }

    /**
     * Gets the value of the buyerProductId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBuyerProductId() {
        return buyerProductId;
    }

    /**
     * Sets the value of the buyerProductId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBuyerProductId(String value) {
        this.buyerProductId = value;
    }

    /**
     * Gets the value of the taricCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTaricCode() {
        return taricCode;
    }

    /**
     * Sets the value of the taricCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTaricCode(String value) {
        this.taricCode = value;
    }

    /**
     * Gets the value of the accounting property.
     *
     * @return
     *     possible object is
     *     {@link AccountingRecord }
     *
     */
    public AccountingRecord getAccounting() {
        return accounting;
    }

    /**
     * Sets the value of the accounting property.
     *
     * @param value
     *     allowed object is
     *     {@link AccountingRecord }
     *
     */
    public void setAccounting(AccountingRecord value) {
        this.accounting = value;
    }

    /**
     * Gets the value of the customerRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCustomerRef() {
        return customerRef;
    }

    /**
     * Sets the value of the customerRef property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCustomerRef(String value) {
        this.customerRef = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the ean property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEAN() {
        return ean;
    }

    /**
     * Sets the value of the ean property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEAN(String value) {
        this.ean = value;
    }

    /**
     * Gets the value of the initialReading property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInitialReading() {
        return initialReading;
    }

    /**
     * Sets the value of the initialReading property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInitialReading(String value) {
        this.initialReading = value;
    }

    /**
     * Gets the value of the finalReading property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFinalReading() {
        return finalReading;
    }

    /**
     * Sets the value of the finalReading property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFinalReading(String value) {
        this.finalReading = value;
    }

    /**
     * Gets the value of the itemReserve property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the itemReserve property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItemReserve().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExtensionRecord }
     *
     *
     */
    public List<ExtensionRecord> getItemReserve() {
        if (itemReserve == null) {
            itemReserve = new ArrayList<ExtensionRecord>();
        }
        return this.itemReserve;
    }

    /**
     * Gets the value of the itemDetailInfo property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the itemDetailInfo property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItemDetailInfo().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ItemDetailInfo }
     *
     *
     */
    public List<ItemDetailInfo> getItemDetailInfo() {
        if (itemDetailInfo == null) {
            itemDetailInfo = new ArrayList<ItemDetailInfo>();
        }
        return this.itemDetailInfo;
    }

    /**
     * Gets the value of the itemSum property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getItemSum() {
        return itemSum;
    }

    /**
     * Sets the value of the itemSum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setItemSum(BigDecimal value) {
        this.itemSum = value;
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
     * {@link AdditionRecord }
     * 
     * 
     */
    public List<AdditionRecord> getAddition() {
        if (addition == null) {
            addition = new ArrayList<AdditionRecord>();
        }
        return this.addition;
    }

    /**
     * Gets the value of the vat property.
     * 
     * @return
     *     possible object is
     *     {@link VATRecord }
     *     
     */
    public VATRecord getVAT() {
        return vat;
    }

    /**
     * Sets the value of the vat property.
     * 
     * @param value
     *     allowed object is
     *     {@link VATRecord }
     *     
     */
    public void setVAT(VATRecord value) {
        this.vat = value;
    }

    /**
     * Gets the value of the itemTotal property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    /**
     * Sets the value of the itemTotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setItemTotal(BigDecimal value) {
        this.itemTotal = value;
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
     *         &lt;element name="ItemUnit" type="{}ShortTextType" minOccurs="0"/>
     *         &lt;element name="ItemAmount" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
     *         &lt;element name="ItemPrice" type="{}Decimal4FractionDigitsType" minOccurs="0"/>
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
        "itemUnit",
        "itemAmount",
        "itemPrice"
    })
    public static class ItemDetailInfo {

        @XmlElement(name = "ItemUnit")
        protected String itemUnit;
        @XmlElement(name = "ItemAmount")
        protected BigDecimal itemAmount;
        @XmlElement(name = "ItemPrice")
        protected BigDecimal itemPrice;

        /**
         * Gets the value of the itemUnit property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getItemUnit() {
            return itemUnit;
        }

        /**
         * Sets the value of the itemUnit property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setItemUnit(String value) {
            this.itemUnit = value;
        }

        /**
         * Gets the value of the itemAmount property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getItemAmount() {
            return itemAmount;
        }

        /**
         * Sets the value of the itemAmount property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setItemAmount(BigDecimal value) {
            this.itemAmount = value;
        }

        /**
         * Gets the value of the itemPrice property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getItemPrice() {
            return itemPrice;
        }

        /**
         * Sets the value of the itemPrice property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setItemPrice(BigDecimal value) {
            this.itemPrice = value;
        }

    }

}
