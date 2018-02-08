package com.axelor.apps.prestashop.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="stock_available")
public class PrestashopAvailableStock extends PrestashopIdentifiableEntity {
	private int productId;
	private int productAttributeId;
	private Integer shopId;
	private Integer shopGroupId;
	private int quantity;
	private boolean dependsOnStock = false;
	private int outOfStock = 2; // Behavior when out of stock, 2 means "use default"

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

	@XmlElement(name="id_shop")
	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	@XmlElement(name="id_shop_group")
	public Integer getShopGroupId() {
		return shopGroupId;
	}

	public void setShopGroupId(Integer shopGroupId) {
		this.shopGroupId = shopGroupId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@XmlElement(name="depends_on_stock")
	public boolean isDependsOnStock() {
		return dependsOnStock;
	}

	public void setDependsOnStock(boolean dependsOnStock) {
		this.dependsOnStock = dependsOnStock;
	}

	@XmlElement(name="out_of_stock")
	public int getOutOfStock() {
		return outOfStock;
	}

	public void setOutOfStock(int outOfStock) {
		this.outOfStock = outOfStock;
	}
}
