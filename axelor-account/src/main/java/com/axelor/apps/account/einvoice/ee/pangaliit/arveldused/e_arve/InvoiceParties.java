
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;


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
 *         &lt;element name="SellerParty" type="{}SellerPartyRecord"/>
 *         &lt;element name="BuyerParty" type="{}BillPartyRecord"/>
 *         &lt;element name="RecipientParty" type="{}BillPartyRecord" minOccurs="0"/>
 *         &lt;element name="DeliveryParty" type="{}BillPartyRecord" minOccurs="0"/>
 *         &lt;element name="PayerParty" type="{}BillPartyRecord" minOccurs="0"/>
 *         &lt;element name="FactorParty" type="{}BillPartyRecord" minOccurs="0"/>
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
    "sellerParty",
    "buyerParty",
    "recipientParty",
    "deliveryParty",
    "payerParty",
    "factorParty"
})
@XmlRootElement(name = "InvoiceParties")
public class InvoiceParties {

    @XmlElement(name = "SellerParty", required = true)
    protected SellerPartyRecord sellerParty;
    @XmlElement(name = "BuyerParty", required = true)
    protected BillPartyRecord buyerParty;
    @XmlElement(name = "RecipientParty")
    protected BillPartyRecord recipientParty;
    @XmlElement(name = "DeliveryParty")
    protected BillPartyRecord deliveryParty;
    @XmlElement(name = "PayerParty")
    protected BillPartyRecord payerParty;
    @XmlElement(name = "FactorParty")
    protected BillPartyRecord factorParty;

    /**
     * Gets the value of the sellerParty property.
     * 
     * @return
     *     possible object is
     *     {@link SellerPartyRecord }
     *     
     */
    public SellerPartyRecord getSellerParty() {
        return sellerParty;
    }

    /**
     * Sets the value of the sellerParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link SellerPartyRecord }
     *     
     */
    public void setSellerParty(SellerPartyRecord value) {
        this.sellerParty = value;
    }

    /**
     * Gets the value of the buyerParty property.
     * 
     * @return
     *     possible object is
     *     {@link BillPartyRecord }
     *     
     */
    public BillPartyRecord getBuyerParty() {
        return buyerParty;
    }

    /**
     * Sets the value of the buyerParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link BillPartyRecord }
     *     
     */
    public void setBuyerParty(BillPartyRecord value) {
        this.buyerParty = value;
    }

    /**
     * Gets the value of the recipientParty property.
     * 
     * @return
     *     possible object is
     *     {@link BillPartyRecord }
     *     
     */
    public BillPartyRecord getRecipientParty() {
        return recipientParty;
    }

    /**
     * Sets the value of the recipientParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link BillPartyRecord }
     *     
     */
    public void setRecipientParty(BillPartyRecord value) {
        this.recipientParty = value;
    }

    /**
     * Gets the value of the deliveryParty property.
     * 
     * @return
     *     possible object is
     *     {@link BillPartyRecord }
     *     
     */
    public BillPartyRecord getDeliveryParty() {
        return deliveryParty;
    }

    /**
     * Sets the value of the deliveryParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link BillPartyRecord }
     *     
     */
    public void setDeliveryParty(BillPartyRecord value) {
        this.deliveryParty = value;
    }

    /**
     * Gets the value of the payerParty property.
     * 
     * @return
     *     possible object is
     *     {@link BillPartyRecord }
     *     
     */
    public BillPartyRecord getPayerParty() {
        return payerParty;
    }

    /**
     * Sets the value of the payerParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link BillPartyRecord }
     *     
     */
    public void setPayerParty(BillPartyRecord value) {
        this.payerParty = value;
    }

    /**
     * Gets the value of the factorParty property.
     * 
     * @return
     *     possible object is
     *     {@link BillPartyRecord }
     *     
     */
    public BillPartyRecord getFactorParty() {
        return factorParty;
    }

    /**
     * Sets the value of the factorParty property.
     * 
     * @param value
     *     allowed object is
     *     {@link BillPartyRecord }
     *     
     */
    public void setFactorParty(BillPartyRecord value) {
        this.factorParty = value;
    }

}
