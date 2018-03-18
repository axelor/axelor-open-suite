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
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Element;

@XmlRootElement(name="currency")
public class PrestashopCurrency extends PrestashopIdentifiableEntity {
	private String name;
	private String code;
	private BigDecimal conversionRate = BigDecimal.ONE;
	private boolean deleted = false;
	private boolean active = true;
	private List<Element> additionalProperties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="iso_code")
	public String getCode() {
		return code;
	}

	public void setCode(String isoCode) {
		this.code = isoCode;
	}

	@XmlElement(name="conversion_rate")
	public BigDecimal getConversionRate() {
		return conversionRate;
	}

	public void setConversionRate(BigDecimal conversionRate) {
		this.conversionRate = conversionRate;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@XmlAnyElement
	public List<Element> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(List<Element> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", name)
				.append("isoCode", code)
				.append("conversionRate", conversionRate)
				.append("deleted", deleted)
				.append("active", active)
				.toString();
	}
}
