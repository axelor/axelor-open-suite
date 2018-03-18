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
