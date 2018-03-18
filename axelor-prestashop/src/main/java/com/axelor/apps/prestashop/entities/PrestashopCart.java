/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.prestashop.entities;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.axelor.apps.prestashop.entities.Associations.CartRowsAssociationsEntry;

/**
 * Represents a cart in prestashop. We need it as every order is
 * bound to a cart in the application…
 */
@XmlRootElement(name="cart")
public class PrestashopCart extends PrestashopIdentifiableEntity {
	private Integer deliveryAddressId;
	private Integer invoiceAddressId;
	private int currencyId;
	private Integer customerId;
	private Integer guestId;
	private int languageId;
	private Integer shopGroupId;
	private Integer shopId;
	private Integer carrierId;
	private boolean recyclable;
	private boolean gift;
	private String giftMessage;
	private boolean mobileTheme;
	private String deliveryOption;
	private String secureKey;
	private boolean separatedPackageAllowed;
	private LocalDateTime addDate = LocalDateTime.now();
	private LocalDateTime updateDate = LocalDateTime.now();
	private Associations associations = new Associations();

	public PrestashopCart() {
		associations.setCartRows(new CartRowsAssociationsEntry());
	}

	@XmlElement(name="id_address_delivery")
	public Integer getDeliveryAddressId() {
		return deliveryAddressId;
	}

	public void setDeliveryAddressId(Integer deliveryAddressId) {
		this.deliveryAddressId = deliveryAddressId;
	}

	@XmlElement(name="id_address_invoice")
	public Integer getInvoiceAddressId() {
		return invoiceAddressId;
	}

	public void setInvoiceAddressId(Integer invoiceAddressId) {
		this.invoiceAddressId = invoiceAddressId;
	}

	@XmlElement(name="id_currency")
	public int getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(int currencyId) {
		this.currencyId = currencyId;
	}

	@XmlElement(name="id_customer")
	public Integer getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}

	@XmlElement(name="id_guest")
	public Integer getGuestId() {
		return guestId;
	}

	public void setGuestId(Integer guestId) {
		this.guestId = guestId;
	}

	@XmlElement(name="id_lang")
	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
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

	@XmlElement(name="id_carrier")
	public Integer getCarrierId() {
		return carrierId;
	}

	public void setCarrierId(Integer carrierId) {
		this.carrierId = carrierId;
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

	@XmlElement(name="delivery_option")
	public String getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	@XmlElement(name="secure_key")
	public String getSecureKey() {
		return secureKey;
	}

	public void setSecureKey(String secureKey) {
		this.secureKey = secureKey;
	}

	@XmlElement(name="allow_seperated_package")
	public boolean isSeparatedPackageAllowed() {
		return separatedPackageAllowed;
	}

	public void setSeparatedPackageAllowed(boolean separatedPackageAllowed) {
		this.separatedPackageAllowed = separatedPackageAllowed;
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

	public Associations getAssociations() {
		return associations;
	}

	public void setAssociations(Associations associations) {
		this.associations = associations;
	}
}
