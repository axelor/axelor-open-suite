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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="order_invoice")
public class PrestashopOrderInvoice extends PrestashopIdentifiableEntity {
	private int orderId;
	private int number;
	private int deliveryNumber;
	private LocalDateTime deliveryDate;
	private BigDecimal totalDiscountsTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalDiscountsTaxExcluded = BigDecimal.ZERO;
	private BigDecimal totalPaidTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalPaidTaxExcluded = BigDecimal.ZERO;
	private BigDecimal totalProductsTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalProductsTaxExcluded = BigDecimal.ZERO;
	private BigDecimal totalShippingTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalShippingTaxExcluded = BigDecimal.ZERO;
	private BigDecimal totalWrappingTaxIncluded = BigDecimal.ZERO;
	private BigDecimal totalWrappingTaxExcluded = BigDecimal.ZERO;
	private Integer shippingTaxComputationMethod;
	private String shopAddress;
	private String note;
	private LocalDateTime addDate = LocalDateTime.now();
	
	@XmlElement(name="id_order")
	public int getOrderId() {
		return orderId;
	}
	
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	@XmlElement(name="delivery_number")
	public int getDeliveryNumber() {
		return deliveryNumber;
	}
	
	public void setDeliveryNumber(int deliveryNumber) {
		this.deliveryNumber = deliveryNumber;
	}

	@XmlElement(name="delivery_date")
	public LocalDateTime getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(LocalDateTime deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	@XmlElement(name="total_discount_tax_incl")
	public BigDecimal getTotalDiscountsTaxIncluded() {
		return totalDiscountsTaxIncluded;
	}

	public void setTotalDiscountsTaxIncluded(BigDecimal totalDiscountsTaxIncluded) {
		this.totalDiscountsTaxIncluded = totalDiscountsTaxIncluded;
	}

	@XmlElement(name="total_discount_tax_excl")
	public BigDecimal getTotalDiscountsTaxExcluded() {
		return totalDiscountsTaxExcluded;
	}

	public void setTotalDiscountsTaxExcluded(BigDecimal totalDiscountsTaxExcluded) {
		this.totalDiscountsTaxExcluded = totalDiscountsTaxExcluded;
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

	@XmlElement(name="total_products_wt")
	public BigDecimal getTotalProductsTaxIncluded() {
		return totalProductsTaxIncluded;
	}

	public void setTotalProductsTaxIncluded(BigDecimal totalProductsTaxIncluded) {
		this.totalProductsTaxIncluded = totalProductsTaxIncluded;
	}

	@XmlElement(name="total_products")
	public BigDecimal getTotalProductsTaxExcluded() {
		return totalProductsTaxExcluded;
	}

	public void setTotalProductsTaxExcluded(BigDecimal totalProductsTaxExcluded) {
		this.totalProductsTaxExcluded = totalProductsTaxExcluded;
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

	@XmlElement(name="shipping_tax_computation_method")
	public Integer getShippingTaxComputationMethod() {
		return shippingTaxComputationMethod;
	}

	public void setShippingTaxComputationMethod(Integer shippingTaxComputationMethod) {
		this.shippingTaxComputationMethod = shippingTaxComputationMethod;
	}

	@XmlElement(name="shop_address")
	public String getShopAddress() {
		return shopAddress;
	}

	public void setShopAddress(String shopAddress) {
		this.shopAddress = shopAddress;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@XmlElement(name="date_add")
	public LocalDateTime getAddDate() {
		return addDate;
	}

	public void setAddDate(LocalDateTime addDate) {
		this.addDate = addDate;
	}
}
