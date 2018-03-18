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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.axelor.apps.prestashop.entities.Associations.OrderRowsAssociationElement;

/**
 * Contains all information describing an order row from
 * Prestashop.
 * @see OrderRowsAssociationElement
 */
@XmlRootElement(name="order_detail")
public class PrestashopOrderRowDetails extends PrestashopIdentifiableEntity {
	private int orderId;
	private Integer productId;
	private Integer productAttributeId;
	private Integer reinjectedProductQuantity;
	private BigDecimal groupReduction;
	private Integer appliedQuantityDiscount;
	private String downloadHash;
	private LocalDateTime downloadDeadline;
	private Integer orderInvoiceId;
	private int warehouseId;
	private int shopId;
	private Integer customizationId;
	private String productName;
	private int productQuantity;
	private int productQuantityInStock;
	private int productQuantityReturn;
	private int productQuantityRefunded;
	@NotNull private BigDecimal productPrice;
	private BigDecimal discountPercent;
	private BigDecimal discountAmount;
	private BigDecimal discountAmountTaxIncluded;
	private BigDecimal discountAmountTaxExcluded;
	private BigDecimal productQuantityDiscount;
	private String ean13;
	private String isbn;
	private String upc;
	private String productReference;
	private String productSupplierReference;
	private BigDecimal productWeight;
	private Integer taxComputationMethod;
	private Integer taxRulesGroupId;
	private BigDecimal ecotax;
	private BigDecimal ecotaxRate;
	private Integer downloadsCount;
	private BigDecimal unitPriceTaxIncluded;
	private BigDecimal unitPriceTaxExcluded;
	private BigDecimal totalPriceTaxIncluded;
	private BigDecimal totalPriceTaxExcluded;
	private BigDecimal totalShippingPriceTaxIncluded;
	private BigDecimal totalShippingPriceTaxExcluded;
	private BigDecimal purchasePrice;
	private BigDecimal originalProductPrice;
	private BigDecimal originalWholeSalePrice;
	private Associations associations;

	@XmlElement(name="id_order")
	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	@XmlElement(name="product_id")
	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	@XmlElement(name="product_attribute_id")
	public Integer getProductAttributeId() {
		return productAttributeId;
	}

	public void setProductAttributeId(Integer productAttributeId) {
		this.productAttributeId = productAttributeId;
	}

	@XmlElement(name="product_quantity_reinjected")
	public Integer getReinjectedProductQuantity() {
		return reinjectedProductQuantity;
	}

	public void setReinjectedProductQuantity(Integer reinjectedProductQuantity) {
		this.reinjectedProductQuantity = reinjectedProductQuantity;
	}

	@XmlElement(name="group_reduction")
	public BigDecimal getGroupReduction() {
		return groupReduction;
	}

	public void setGroupReduction(BigDecimal groupReduction) {
		this.groupReduction = groupReduction;
	}

	@XmlElement(name="discount_quantity_applied")
	public Integer getAppliedQuantityDiscount() {
		return appliedQuantityDiscount;
	}

	public void setAppliedQuantityDiscount(Integer appliedQuantityDiscount) {
		this.appliedQuantityDiscount = appliedQuantityDiscount;
	}

	@XmlElement(name="download_hash")
	public String getDownloadHash() {
		return downloadHash;
	}

	public void setDownloadHash(String downloadHash) {
		this.downloadHash = downloadHash;
	}

	@XmlElement(name="download_deadline")
	public LocalDateTime getDownloadDeadline() {
		return downloadDeadline;
	}

	public void setDownloadDeadline(LocalDateTime downloadDeadline) {
		this.downloadDeadline = downloadDeadline;
	}

	@XmlElement(name="id_order_invoice")
	public Integer getOrderInvoiceId() {
		return orderInvoiceId;
	}

	public void setOrderInvoiceId(Integer orderInvoiceId) {
		this.orderInvoiceId = orderInvoiceId;
	}

	@XmlElement(name="id_warehouse")
	public int getWarehouseId() {
		return warehouseId;
	}

	public void setWarehouseId(int warehouseId) {
		this.warehouseId = warehouseId;
	}

	@XmlElement(name="id_shop")
	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	@XmlElement(name="id_customization")
	public Integer getCustomizationId() {
		return customizationId;
	}

	public void setCustomizationId(Integer customizationId) {
		this.customizationId = customizationId;
	}

	@XmlElement(name="product_name")
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	@XmlElement(name="product_quantity")
	public int getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(int productQuantity) {
		this.productQuantity = productQuantity;
	}

	@XmlElement(name="product_quantity_in_stock")
	public int getProductQuantityInStock() {
		return productQuantityInStock;
	}

	public void setProductQuantityInStock(int productQuantityInStock) {
		this.productQuantityInStock = productQuantityInStock;
	}

	@XmlElement(name="product_quantity_return")
	public int getProductQuantityReturn() {
		return productQuantityReturn;
	}

	public void setProductQuantityReturn(int productQuantityReturn) {
		this.productQuantityReturn = productQuantityReturn;
	}

	@XmlElement(name="product_quantity_refunded")
	public int getProductQuantityRefunded() {
		return productQuantityRefunded;
	}

	public void setProductQuantityRefunded(int productQuantityRefunded) {
		this.productQuantityRefunded = productQuantityRefunded;
	}

	@XmlElement(name="product_price")
	public BigDecimal getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	@XmlElement(name="reduction_percent")
	public BigDecimal getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
	}

	@XmlElement(name="reduction_amount")
	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	@XmlElement(name="reduction_amount_tax_incl")
	public BigDecimal getDiscountAmountTaxIncluded() {
		return discountAmountTaxIncluded;
	}

	public void setDiscountAmountTaxIncluded(BigDecimal discountAmountTaxIncluded) {
		this.discountAmountTaxIncluded = discountAmountTaxIncluded;
	}

	@XmlElement(name="reduction_amount_tax_excl")
	public BigDecimal getDiscountAmountTaxExcluded() {
		return discountAmountTaxExcluded;
	}

	public void setDiscountAmountTaxExcluded(BigDecimal discountAmountTaxExcluded) {
		this.discountAmountTaxExcluded = discountAmountTaxExcluded;
	}

	@XmlElement(name="product_quantity_discount")
	public BigDecimal getProductQuantityDiscount() {
		return productQuantityDiscount;
	}

	public void setProductQuantityDiscount(BigDecimal productQuantityDiscount) {
		this.productQuantityDiscount = productQuantityDiscount;
	}

	@XmlElement(name="product_ean13")
	public String getEan13() {
		return ean13;
	}

	public void setEan13(String ean13) {
		this.ean13 = ean13;
	}

	@XmlElement(name="product_isbn")
	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	@XmlElement(name="product_upc")
	public String getUpc() {
		return upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	@XmlElement(name="product_reference")
	public String getProductReference() {
		return productReference;
	}

	public void setProductReference(String productReference) {
		this.productReference = productReference;
	}

	@XmlElement(name="product_supplier_reference")
	public String getProductSupplierReference() {
		return productSupplierReference;
	}

	public void setProductSupplierReference(String productSupplierReference) {
		this.productSupplierReference = productSupplierReference;
	}

	@XmlElement(name="product_weight")
	public BigDecimal getProductWeight() {
		return productWeight;
	}

	public void setProductWeight(BigDecimal productWeight) {
		this.productWeight = productWeight;
	}

	@XmlElement(name="tax_computation_method")
	public Integer getTaxComputationMethod() {
		return taxComputationMethod;
	}

	public void setTaxComputationMethod(Integer taxComputationMethod) {
		this.taxComputationMethod = taxComputationMethod;
	}

	@XmlElement(name="id_tax_rules_group")
	public Integer getTaxRulesGroupId() {
		return taxRulesGroupId;
	}

	public void setTaxRulesGroupId(Integer taxRulesGroupId) {
		this.taxRulesGroupId = taxRulesGroupId;
	}

	public BigDecimal getEcotax() {
		return ecotax;
	}

	public void setEcotax(BigDecimal ecotax) {
		this.ecotax = ecotax;
	}

	@XmlElement(name="ecotax_tax_rate")
	public BigDecimal getEcotaxRate() {
		return ecotaxRate;
	}

	public void setEcotaxRate(BigDecimal ecotaxRate) {
		this.ecotaxRate = ecotaxRate;
	}

	@XmlElement(name="download_nb")
	public Integer getDownloadsCount() {
		return downloadsCount;
	}

	public void setDownloadsCount(Integer downloadsCount) {
		this.downloadsCount = downloadsCount;
	}

	@XmlElement(name="unit_price_tax_incl")
	public BigDecimal getUnitPriceTaxIncluded() {
		return unitPriceTaxIncluded;
	}

	public void setUnitPriceTaxIncluded(BigDecimal unitPriceTaxIncluded) {
		this.unitPriceTaxIncluded = unitPriceTaxIncluded;
	}

	@XmlElement(name="unit_price_tax_excl")
	public BigDecimal getUnitPriceTaxExcluded() {
		return unitPriceTaxExcluded;
	}

	public void setUnitPriceTaxExcluded(BigDecimal unitPriceTaxExcluded) {
		this.unitPriceTaxExcluded = unitPriceTaxExcluded;
	}

	@XmlElement(name="total_price_tax_incl")
	public BigDecimal getTotalPriceTaxIncluded() {
		return totalPriceTaxIncluded;
	}

	public void setTotalPriceTaxIncluded(BigDecimal totalPriceTaxIncluded) {
		this.totalPriceTaxIncluded = totalPriceTaxIncluded;
	}

	@XmlElement(name="total_price_tax_excl")
	public BigDecimal getTotalPriceTaxExcluded() {
		return totalPriceTaxExcluded;
	}

	public void setTotalPriceTaxExcluded(BigDecimal totalPriceTaxExcluded) {
		this.totalPriceTaxExcluded = totalPriceTaxExcluded;
	}

	@XmlElement(name="total_shipping_price_tax_excl")
	public BigDecimal getTotalShippingPriceTaxIncluded() {
		return totalShippingPriceTaxIncluded;
	}

	public void setTotalShippingPriceTaxIncluded(BigDecimal totalShippingPriceTaxIncluded) {
		this.totalShippingPriceTaxIncluded = totalShippingPriceTaxIncluded;
	}

	@XmlElement(name="total_shipping_price_tax_incl")
	public BigDecimal getTotalShippingPriceTaxExcluded() {
		return totalShippingPriceTaxExcluded;
	}

	public void setTotalShippingPriceTaxExcluded(BigDecimal totalShippingPriceTaxExcluded) {
		this.totalShippingPriceTaxExcluded = totalShippingPriceTaxExcluded;
	}

	@XmlElement(name="purchase_supplier_price")
	public BigDecimal getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(BigDecimal purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	@XmlElement(name="original_product_price")
	public BigDecimal getOriginalProductPrice() {
		return originalProductPrice;
	}

	public void setOriginalProductPrice(BigDecimal originalProductPrice) {
		this.originalProductPrice = originalProductPrice;
	}

	@XmlElement(name="original_wholesale_price")
	public BigDecimal getOriginalWholeSalePrice() {
		return originalWholeSalePrice;
	}

	public void setOriginalWholeSalePrice(BigDecimal originalWholeSalePrice) {
		this.originalWholeSalePrice = originalWholeSalePrice;
	}

	public Associations getAssociations() {
		return associations;
	}

	public void setAssociations(Associations associations) {
		this.associations = associations;
	}
}
