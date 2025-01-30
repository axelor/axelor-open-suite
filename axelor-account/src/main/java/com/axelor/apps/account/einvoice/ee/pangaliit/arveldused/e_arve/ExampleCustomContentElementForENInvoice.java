
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{}InvoiceInformationEN" minOccurs="0"/>
 *         &lt;element ref="{}InvoiceSumGroupEN" minOccurs="0"/>
 *         &lt;element ref="{}PaymentInfoEN" minOccurs="0"/>
 *         &lt;element ref="{}PartyEN" minOccurs="0"/>
 *         &lt;element ref="{}AdditionEN" minOccurs="0"/>
 *         &lt;element ref="{}ItemEntryEN" minOccurs="0"/>
 *         &lt;element ref="{}MailAddressEN" minOccurs="0"/>
 *         &lt;element ref="{}VATEN" minOccurs="0"/>
 *         &lt;element ref="{}SellerTaxRepPartyEN" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "invoiceInformationEN",
    "invoiceSumGroupEN",
    "paymentInfoEN",
    "partyEN",
    "additionEN",
    "itemEntryEN",
    "mailAddressEN",
    "vaten",
    "sellerTaxRepPartyEN"
})
@XmlRootElement(name = "ExampleCustomContentElementForENInvoice")
public class ExampleCustomContentElementForENInvoice {

    @XmlElement(name = "InvoiceInformationEN")
    protected InvoiceInformationENType invoiceInformationEN;
    @XmlElement(name = "InvoiceSumGroupEN")
    protected InvoiceSumGroupENType invoiceSumGroupEN;
    @XmlElement(name = "PaymentInfoEN")
    protected PaymentInfoENType paymentInfoEN;
    @XmlElement(name = "PartyEN")
    protected PartyENType partyEN;
    @XmlElement(name = "AdditionEN")
    protected AdditionENType additionEN;
    @XmlElement(name = "ItemEntryEN")
    protected ItemEntryENType itemEntryEN;
    @XmlElement(name = "MailAddressEN")
    protected MailAddressENType mailAddressEN;
    @XmlElement(name = "VATEN")
    protected VATENType vaten;
    @XmlElement(name = "SellerTaxRepPartyEN")
    protected BillPartyRecord sellerTaxRepPartyEN;

    /**
     * Gets the value of the invoiceInformationEN property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceInformationENType }
     *     
     */
    public InvoiceInformationENType getInvoiceInformationEN() {
        return invoiceInformationEN;
    }

    /**
     * Sets the value of the invoiceInformationEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceInformationENType }
     *     
     */
    public void setInvoiceInformationEN(InvoiceInformationENType value) {
        this.invoiceInformationEN = value;
    }

    /**
     * Gets the value of the invoiceSumGroupEN property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceSumGroupENType }
     *     
     */
    public InvoiceSumGroupENType getInvoiceSumGroupEN() {
        return invoiceSumGroupEN;
    }

    /**
     * Sets the value of the invoiceSumGroupEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceSumGroupENType }
     *     
     */
    public void setInvoiceSumGroupEN(InvoiceSumGroupENType value) {
        this.invoiceSumGroupEN = value;
    }

    /**
     * Gets the value of the paymentInfoEN property.
     * 
     * @return
     *     possible object is
     *     {@link PaymentInfoENType }
     *     
     */
    public PaymentInfoENType getPaymentInfoEN() {
        return paymentInfoEN;
    }

    /**
     * Sets the value of the paymentInfoEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentInfoENType }
     *     
     */
    public void setPaymentInfoEN(PaymentInfoENType value) {
        this.paymentInfoEN = value;
    }

    /**
     * Gets the value of the partyEN property.
     * 
     * @return
     *     possible object is
     *     {@link PartyENType }
     *     
     */
    public PartyENType getPartyEN() {
        return partyEN;
    }

    /**
     * Sets the value of the partyEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartyENType }
     *     
     */
    public void setPartyEN(PartyENType value) {
        this.partyEN = value;
    }

    /**
     * Gets the value of the additionEN property.
     * 
     * @return
     *     possible object is
     *     {@link AdditionENType }
     *     
     */
    public AdditionENType getAdditionEN() {
        return additionEN;
    }

    /**
     * Sets the value of the additionEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdditionENType }
     *     
     */
    public void setAdditionEN(AdditionENType value) {
        this.additionEN = value;
    }

    /**
     * Gets the value of the itemEntryEN property.
     * 
     * @return
     *     possible object is
     *     {@link ItemEntryENType }
     *     
     */
    public ItemEntryENType getItemEntryEN() {
        return itemEntryEN;
    }

    /**
     * Sets the value of the itemEntryEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemEntryENType }
     *     
     */
    public void setItemEntryEN(ItemEntryENType value) {
        this.itemEntryEN = value;
    }

    /**
     * Gets the value of the mailAddressEN property.
     * 
     * @return
     *     possible object is
     *     {@link MailAddressENType }
     *     
     */
    public MailAddressENType getMailAddressEN() {
        return mailAddressEN;
    }

    /**
     * Sets the value of the mailAddressEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailAddressENType }
     *     
     */
    public void setMailAddressEN(MailAddressENType value) {
        this.mailAddressEN = value;
    }

    /**
     * Gets the value of the vaten property.
     * 
     * @return
     *     possible object is
     *     {@link VATENType }
     *     
     */
    public VATENType getVATEN() {
        return vaten;
    }

    /**
     * Sets the value of the vaten property.
     * 
     * @param value
     *     allowed object is
     *     {@link VATENType }
     *     
     */
    public void setVATEN(VATENType value) {
        this.vaten = value;
    }

    /**
     * Gets the value of the sellerTaxRepPartyEN property.
     * 
     * @return
     *     possible object is
     *     {@link BillPartyRecord }
     *     
     */
    public BillPartyRecord getSellerTaxRepPartyEN() {
        return sellerTaxRepPartyEN;
    }

    /**
     * Sets the value of the sellerTaxRepPartyEN property.
     * 
     * @param value
     *     allowed object is
     *     {@link BillPartyRecord }
     *     
     */
    public void setSellerTaxRepPartyEN(BillPartyRecord value) {
        this.sellerTaxRepPartyEN = value;
    }

}
