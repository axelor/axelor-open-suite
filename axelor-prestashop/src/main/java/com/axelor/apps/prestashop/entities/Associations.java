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
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.Element;

import com.axelor.apps.prestashop.adapters.StandardBooleanAdapter;

public class Associations {
	private ImagesAssociationsEntry images;
	private AvailableStocksAssociationsEntry availableStocks;
	private CategoriesAssociationsEntry categories;
	private CartRowsAssociationsEntry cartRows;
	private OrderRowsAssociationsEntry orderRows;
	// Warning, if you ever add ProductBundle handling, adjust PrestashopProduct filtering
	// too remove it on virtual products…
	private List<Element> additionalEntries;

	public ImagesAssociationsEntry getImages() {
		return images;
	}

	public void setImages(ImagesAssociationsEntry images) {
		this.images = images;
	}

	@XmlElement(name="stock_availables")
	public AvailableStocksAssociationsEntry getAvailableStocks() {
		return availableStocks;
	}

	public void setAvailableStocks(AvailableStocksAssociationsEntry availableStocks) {
		this.availableStocks = availableStocks;
	}

	public CategoriesAssociationsEntry getCategories() {
		return categories;
	}

	public void setCategories(CategoriesAssociationsEntry categories) {
		this.categories = categories;
	}

	@XmlElement(name="cart_rows")
	public CartRowsAssociationsEntry getCartRows() {
		return cartRows;
	}

	public void setCartRows(CartRowsAssociationsEntry cartRows) {
		this.cartRows = cartRows;
	}

	@XmlElement(name="order_rows")
	public OrderRowsAssociationsEntry getOrderRows() {
		return orderRows;
	}

	public void setOrderRows(OrderRowsAssociationsEntry orderRows) {
		this.orderRows = orderRows;
	}

	@XmlAnyElement
	public List<Element> getAdditionalEntries() {
		return additionalEntries;
	}

	public void setAdditionalEntries(List<Element> additionalEntries) {
		this.additionalEntries = additionalEntries;
	}

	public static abstract class AssociationsEntry {
		@XmlAttribute
		protected String nodeType;
		@XmlAttribute
		protected String api;

		// Just for JAXB to stop complain
		@SuppressWarnings("unused")
		private AssociationsEntry() {}

		public AssociationsEntry(String nodeType, String api) {
			this.nodeType = nodeType;
			this.api = api;
		}
	}

	public static class ImagesAssociationsEntry extends AssociationsEntry {
		private List<ImagesAssociationElement> images = new LinkedList<>();

		public ImagesAssociationsEntry() {
			super("image", "images");
		}

		@XmlElement(name="image")
		public List<ImagesAssociationElement> getImages() {
			return images;
		}

		public void setImages(List<ImagesAssociationElement> images) {
			this.images = images;
		}
	}

	public static class AvailableStocksAssociationsEntry extends AssociationsEntry {
		private List<AvailableStocksAssociationElement> stocks = new LinkedList<>();

		public AvailableStocksAssociationsEntry() {
			super("stock_available", "stock_availables");
		}

		@XmlElement(name="stock_available")
		public List<AvailableStocksAssociationElement> getStock() {
			return stocks;
		}

		public void setStock(List<AvailableStocksAssociationElement> stocks) {
			this.stocks = stocks;
		}
	}

	public static class CategoriesAssociationsEntry extends AssociationsEntry {
		private List<CategoriesAssociationElement> associations = new LinkedList<>();

		public CategoriesAssociationsEntry() {
			super("category", "categories");
		}

		@XmlElement(name="category")
		public List<CategoriesAssociationElement> getAssociations() {
			return associations;
		}

		public void setAssociations(List<CategoriesAssociationElement> associations) {
			this.associations = associations;
		}
	}

	public static class CartRowsAssociationsEntry extends AssociationsEntry {
		private List<CartRowsAssociationElement> cartRows = new LinkedList<>();
		private boolean virtualEntity = true;

		public CartRowsAssociationsEntry() {
			super("cart_row", null);
		}

		@XmlAttribute
		@XmlJavaTypeAdapter(type=boolean.class, value=StandardBooleanAdapter.class)
		public boolean isVirtualEntity() {
			return virtualEntity;
		}

		public void setVirtualEntity(boolean virtualEntity) {
			this.virtualEntity = virtualEntity;
		}

		@XmlElement(name="cart_row")
		public List<CartRowsAssociationElement> getCartRows() {
			return cartRows;
		}

		public void setCartRows(List<CartRowsAssociationElement> cartRows) {
			this.cartRows = cartRows;
		}
	}

	public static class OrderRowsAssociationsEntry extends AssociationsEntry {
		private List<OrderRowsAssociationElement> orderRows = new LinkedList<>();
		private boolean virtualEntity = true;

		public OrderRowsAssociationsEntry() {
			super("order_row", null);
		}

		@XmlAttribute
		@XmlJavaTypeAdapter(type=boolean.class, value=StandardBooleanAdapter.class)
		public boolean isVirtualEntity() {
			return virtualEntity;
		}

		public void setVirtualEntity(boolean virtualEntity) {
			this.virtualEntity = virtualEntity;
		}

		@XmlElement(name="order_row")
		public List<OrderRowsAssociationElement> getOrderRows() {
			return orderRows;
		}

		public void setOrderRows(List<OrderRowsAssociationElement> orderRows) {
			this.orderRows = orderRows;
		}
	}

	public static class ImagesAssociationElement {
		private String href;
		private Integer id;

		@XmlAttribute(name="href", namespace="http://www.w3.org/1999/xlink")
		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
	}

	public static class AvailableStocksAssociationElement {
		private String href;
		private Integer id;
		private Integer productAttributeId;

		@XmlAttribute(name="href", namespace="http://www.w3.org/1999/xlink")
		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@XmlElement(name="id_product_attribute")
		public Integer getProductAttributeId() {
			return productAttributeId;
		}

		public void setProductAttributeId(Integer productAttributeId) {
			this.productAttributeId = productAttributeId;
		}
	}

	public static class CategoriesAssociationElement {
		private String href;
		private Integer id;

		@XmlAttribute(name="href", namespace="http://www.w3.org/1999/xlink")
		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
	}

	public static class CartRowsAssociationElement {
		private int productId;
		private int productAttributeId;
		private int deliveryAddressId;
		private int quantity;

		@XmlElement(name="id_product")
		public int getProductId() {
			return productId;
		}

		public void setProductId(int productId) {
			this.productId = productId;
		}

		@XmlElement(name="id_product_attribute")
		public int getProductAttributeId() {
			return productAttributeId;
		}

		public void setProductAttributeId(int productAttributeId) {
			this.productAttributeId = productAttributeId;
		}

		@XmlElement(name="id_address_delivery")
		public int getDeliveryAddressId() {
			return deliveryAddressId;
		}

		public void setDeliveryAddressId(int deliveryAddressId) {
			this.deliveryAddressId = deliveryAddressId;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

	public static class OrderRowsAssociationElement {
		private Integer id;
		private int productId;
		private int productAttributeId;
		private int quantity;
		// All remaining attributes are readonly, but we want
		// to be able to unmarshal them
		private String productName;
		private String productReference;
		private String ean13;
		private String isbn;
		private String upc;
		private BigDecimal productPrice;
		private BigDecimal unitPriceTaxIncluded;
		private BigDecimal unitPriceTaxExcluded;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@XmlElement(name="product_id")
		public int getProductId() {
			return productId;
		}

		public void setProductId(int productId) {
			this.productId = productId;
		}

		@XmlElement(name="product_attribute_id")
		public int getProductAttributeId() {
			return productAttributeId;
		}

		public void setProductAttributeId(int productAttributeId) {
			this.productAttributeId = productAttributeId;
		}

		@XmlElement(name="product_quantity")
		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		@XmlTransient
		public String getProductName() {
			return productName;
		}

		@XmlElement(name="product_name")
		private String getProductNameInternal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setProductNameInternal(String productName) {
			this.productName = productName;
		}

		@XmlTransient
		public String getProductReference() {
			return productReference;
		}

		@XmlElement(name="product_reference")
		private String getProductReferenceInternal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setProductReferenceInternal(String productReference) {
			this.productReference = productReference;
		}

		@XmlTransient
		public String getEan13() {
			return ean13;
		}

		@XmlElement(name="product_ean13")
		private String getEan13Internal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setEan13Internal(String ean13) {
			this.ean13 = ean13;
		}

		@XmlTransient
		public String getIsbn() {
			return isbn;
		}

		@XmlElement(name="product_isbn")
		private String getIsbnInternal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setIsbnInternal(String isbn) {
			this.isbn = isbn;
		}

		@XmlTransient
		public String getUpc() {
			return upc;
		}

		@XmlElement(name="product_upc")
		private String getUpcInternal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setUpcInternal(String upc) {
			this.upc = upc;
		}

		@XmlTransient
		public BigDecimal getProductPrice() {
			return productPrice;
		}

		@XmlElement(name="product_price")
		private BigDecimal getProductPriceInternal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setProductPriceInternal(BigDecimal productPrice) {
			this.productPrice = productPrice;
		}

		@XmlTransient
		public BigDecimal getUnitPriceTaxIncluded() {
			return unitPriceTaxIncluded;
		}

		@XmlElement(name="unit_price_tax_incl")
		private BigDecimal getUnitPriceTaxIncludedInternal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setUnitPriceTaxIncludedInternal(BigDecimal unitPriceTaxIncluded) {
			this.unitPriceTaxIncluded = unitPriceTaxIncluded;
		}

		@XmlTransient
		public BigDecimal getUnitPriceTaxExcluded() {
			return unitPriceTaxExcluded;
		}

		@XmlElement(name="unit_price_tax_excl")
		public BigDecimal getUnitPriceTaxExcludedInternal() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setUnitPriceTaxExcludedInternal(BigDecimal unitPriceTaxExcluded) {
			this.unitPriceTaxExcluded = unitPriceTaxExcluded;
		}
	}
}
