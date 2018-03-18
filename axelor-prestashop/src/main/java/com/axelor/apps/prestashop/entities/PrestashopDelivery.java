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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="delivery")
public class PrestashopDelivery extends PrestashopIdentifiableEntity {
	private int carrierId;
	private int priceRangeId;
	private int weightRangeId;
	private int zoneId;
	private Integer shopId;
	private Integer shopGroupId;
	private BigDecimal price;

	@XmlElement(name="id_carrier")
	public int getCarrierId() {
		return carrierId;
	}

	public void setCarrierId(int carrierId) {
		this.carrierId = carrierId;
	}

	@XmlElement(name="id_range_price")
	public int getPriceRangeId() {
		return priceRangeId;
	}

	public void setPriceRangeId(int priceRangeId) {
		this.priceRangeId = priceRangeId;
	}

	@XmlElement(name="id_range_weight")
	public int getWeightRangeId() {
		return weightRangeId;
	}

	public void setWeightRangeId(int weightRangeId) {
		this.weightRangeId = weightRangeId;
	}

	@XmlElement(name="id_zone")
	public int getZoneId() {
		return zoneId;
	}

	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}
