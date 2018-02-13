package com.axelor.apps.prestashop.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="order")
public class PrestashopOrder extends PrestashopIdentifiableEntity {
	private int deliveryAddressId;
	private int invoiceAddressId;
	private int cartId;
	private int currencyId;
	private int languageId;
	private int customerId;
	private int carrierId;
	private Integer currentState;
	private String module;
	private Integer invoiceNumber;
	private LocalDateTime invoiceDate;
	private Integer deliveryNumber;
	private LocalDateTime deliveryDate;
	private boolean valid;
	private LocalDateTime addDate = LocalDateTime.now();
	private LocalDateTime updateDate = LocalDateTime.now();
	private String shippingNumber;
	private Integer shopGroupId;
	private Integer shopId;
	private String secureKey;
	private String payment;
	private boolean recyclable;
	private boolean gift;
	private String giftMessage;
	private boolean mobileTheme;
	private BigDecimal totalDiscounts = BigDecimal.ZERO;
	private BigDecimal totalDiscountsTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalDiscountsTaxExcluded = BigDecimal.ZERO;
	private BigDecimal totalPaid = BigDecimal.ZERO;
	private BigDecimal totalPaidTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalPaidTaxExcluded = BigDecimal.ZERO;
	private BigDecimal totalPaidReal = BigDecimal.ZERO;
	private BigDecimal totalProductsTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalProductsTaxExcluded = BigDecimal.ZERO;
	private BigDecimal totalShipping = BigDecimal.ZERO;
	private BigDecimal totalShippingTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalShippingTaxExcluded = BigDecimal.ZERO;
	private BigDecimal carrierTaxRate = BigDecimal.ZERO;
	private BigDecimal totalWrapping = BigDecimal.ZERO;
	private BigDecimal totalWrappingTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalWrappingTaxExcluded = BigDecimal.ZERO;
	private Integer roundMode;
	private Integer roundType;
	private BigDecimal conversionRate = BigDecimal.ONE;
	private String reference;
	private Associations associations;

	@XmlElement(name="id_address_delivery")
	public int getDeliveryAddressId() {
		return deliveryAddressId;
	}

	public void setDeliveryAddressId(int deliveryAddressId) {
		this.deliveryAddressId = deliveryAddressId;
	}

	@XmlElement(name="id_address_invoice")
	public int getInvoiceAddressId() {
		return invoiceAddressId;
	}

	public void setInvoiceAddressId(int invoiceAddressId) {
		this.invoiceAddressId = invoiceAddressId;
	}

	@XmlElement(name="id_cart")
	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	@XmlElement(name="id_currency")
	public int getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(int currencyId) {
		this.currencyId = currencyId;
	}

	@XmlElement(name="id_lang")
	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	@XmlElement(name="id_customer")
	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	@XmlElement(name="id_carrier")
	public int getCarrierId() {
		return carrierId;
	}

	public void setCarrierId(int carrierId) {
		this.carrierId = carrierId;
	}

	@XmlElement(name="current_state")
	public Integer getCurrentState() {
		return currentState;
	}

	public void setCurrentState(Integer currentState) {
		this.currentState = currentState;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	@XmlElement(name="invoice_number")
	public Integer getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(Integer invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	@XmlElement(name="invoice_date")
	public LocalDateTime getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDateTime invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	@XmlElement(name="delivery_number")
	public Integer getDeliveryNumber() {
		return deliveryNumber;
	}

	public void setDeliveryNumber(Integer deliveryNumber) {
		this.deliveryNumber = deliveryNumber;
	}

	@XmlElement(name="delivery_date")
	public LocalDateTime getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(LocalDateTime deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@XmlElement(name="date_add")
	public LocalDateTime getAddDate() {
		return addDate;
	}

	public void setAddDate(LocalDateTime addDate) {
		this.addDate = addDate;
	}

	@XmlElement(name="date_upd")
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@XmlElement(name="shipping_number")
	public String getShippingNumber() {
		return shippingNumber;
	}

	public void setShippingNumber(String shippingNumber) {
		this.shippingNumber = shippingNumber;
	}

	@XmlElement(name="id_shop_group")
	public Integer getShopGroupId() {
		return shopGroupId;
	}

	public void setShopGroupId(Integer shopGroupId) {
		this.shopGroupId = shopGroupId;
	}

	@XmlElement(name="id_shop")
	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	@XmlElement(name="secure_key")
	public String getSecureKey() {
		return secureKey;
	}

	public void setSecureKey(String secureKey) {
		this.secureKey = secureKey;
	}

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
	}

	public boolean isRecyclable() {
		return recyclable;
	}

	public void setRecyclable(boolean recyclable) {
		this.recyclable = recyclable;
	}

	public boolean isGift() {
		return gift;
	}

	public void setGift(boolean gift) {
		this.gift = gift;
	}

	@XmlElement(name="gift_message")
	public String getGiftMessage() {
		return giftMessage;
	}

	public void setGiftMessage(String giftMessage) {
		this.giftMessage = giftMessage;
	}

	@XmlElement(name="mobile_theme")
	public boolean isMobileTheme() {
		return mobileTheme;
	}

	public void setMobileTheme(boolean mobileTheme) {
		this.mobileTheme = mobileTheme;
	}

	@XmlElement(name="total_discounts")
	public BigDecimal getTotalDiscounts() {
		return totalDiscounts;
	}

	public void setTotalDiscounts(BigDecimal totalDiscounts) {
		this.totalDiscounts = totalDiscounts;
	}

	@XmlElement(name="total_discounts_tax_incl")
	public BigDecimal getTotalDiscountsTaxIncluded() {
		return totalDiscountsTaxIncluded;
	}

	public void setTotalDiscountsTaxIncluded(BigDecimal totalDiscountsTaxIncluded) {
		this.totalDiscountsTaxIncluded = totalDiscountsTaxIncluded;
	}

	@XmlElement(name="total_discounts_tax_excl")
	public BigDecimal getTotalDiscountsTaxExcluded() {
		return totalDiscountsTaxExcluded;
	}

	public void setTotalDiscountsTaxExcluded(BigDecimal totalDiscountsTaxExcluded) {
		this.totalDiscountsTaxExcluded = totalDiscountsTaxExcluded;
	}

	@XmlElement(name="total_paid")
	public BigDecimal getTotalPaid() {
		return totalPaid;
	}

	public void setTotalPaid(BigDecimal totalPaid) {
		this.totalPaid = totalPaid;
	}

	@XmlElement(name="total_paid_tax_incl")
	public BigDecimal getTotalPaidTaxIncluded() {
		return totalPaidTaxIncluded;
	}

	public void setTotalPaidTaxIncluded(BigDecimal totalPaidTaxIncluded) {
		this.totalPaidTaxIncluded = totalPaidTaxIncluded;
	}

	@XmlElement(name="total_paid_tax_excl")
	public BigDecimal getTotalPaidTaxExcluded() {
		return totalPaidTaxExcluded;
	}

	public void setTotalPaidTaxExcluded(BigDecimal totalPaidTaxExcluded) {
		this.totalPaidTaxExcluded = totalPaidTaxExcluded;
	}

	@XmlElement(name="total_paid_real")
	public BigDecimal getTotalPaidReal() {
		return totalPaidReal;
	}

	public void setTotalPaidReal(BigDecimal totalPaidReal) {
		this.totalPaidReal = totalPaidReal;
	}

	@XmlElement(name="total_products")
	public BigDecimal getTotalProductsTaxIncluded() {
		return totalProductsTaxIncluded;
	}

	public void setTotalProductsTaxIncluded(BigDecimal totalProductsTaxIncluded) {
		this.totalProductsTaxIncluded = totalProductsTaxIncluded;
	}

	@XmlElement(name="total_products_wt")
	public BigDecimal getTotalProductsTaxExcluded() {
		return totalProductsTaxExcluded;
	}

	public void setTotalProductsTaxExcluded(BigDecimal totalProductsTaxExcluded) {
		this.totalProductsTaxExcluded = totalProductsTaxExcluded;
	}

	@XmlElement(name="total_shipping")
	public BigDecimal getTotalShipping() {
		return totalShipping;
	}

	public void setTotalShipping(BigDecimal totalShipping) {
		this.totalShipping = totalShipping;
	}

	@XmlElement(name="total_shipping_tax_incl")
	public BigDecimal getTotalShippingTaxIncluded() {
		return totalShippingTaxIncluded;
	}

	public void setTotalShippingTaxIncluded(BigDecimal totalShippingTaxIncluded) {
		this.totalShippingTaxIncluded = totalShippingTaxIncluded;
	}

	@XmlElement(name="total_shipping_tax_excl")
	public BigDecimal getTotalShippingTaxExcluded() {
		return totalShippingTaxExcluded;
	}

	public void setTotalShippingTaxExcluded(BigDecimal totalShippingTaxExcluded) {
		this.totalShippingTaxExcluded = totalShippingTaxExcluded;
	}

	@XmlElement(name="carrier_tax_rate")
	public BigDecimal getCarrierTaxRate() {
		return carrierTaxRate;
	}

	public void setCarrierTaxRate(BigDecimal carrierTaxRate) {
		this.carrierTaxRate = carrierTaxRate;
	}

	@XmlElement(name="total_wrapping")
	public BigDecimal getTotalWrapping() {
		return totalWrapping;
	}

	public void setTotalWrapping(BigDecimal totalWrapping) {
		this.totalWrapping = totalWrapping;
	}

	@XmlElement(name="total_wrapping_tax_incl")
	public BigDecimal getTotalWrappingTaxIncluded() {
		return totalWrappingTaxIncluded;
	}

	public void setTotalWrappingTaxIncluded(BigDecimal totalWrappingTaxIncluded) {
		this.totalWrappingTaxIncluded = totalWrappingTaxIncluded;
	}

	@XmlElement(name="total_wrapping_tax_excl")
	public BigDecimal getTotalWrappingTaxExcluded() {
		return totalWrappingTaxExcluded;
	}

	public void setTotalWrappingTaxExcluded(BigDecimal totalWrappingTaxExcluded) {
		this.totalWrappingTaxExcluded = totalWrappingTaxExcluded;
	}

	@XmlElement(name="round_mode")
	public Integer getRoundMode() {
		return roundMode;
	}

	public void setRoundMode(Integer roundMode) {
		this.roundMode = roundMode;
	}

	@XmlElement(name="round_type")
	public Integer getRoundType() {
		return roundType;
	}

	public void setRoundType(Integer roundType) {
		this.roundType = roundType;
	}

	@XmlElement(name="conversion_rate")
	public BigDecimal getConversionRate() {
		return conversionRate;
	}

	public void setConversionRate(BigDecimal conversionRate) {
		this.conversionRate = conversionRate;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Associations getAssociations() {
		return associations;
	}

	public void setAssociations(Associations associations) {
		this.associations = associations;
	}
}
