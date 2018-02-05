package com.axelor.apps.prestashop.entities;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Element;

import com.axelor.apps.prestashop.adapters.PrestashopBooleanAdapter;

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

	@XmlJavaTypeAdapter(type=boolean.class, value=PrestashopBooleanAdapter.class)
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@XmlJavaTypeAdapter(type=boolean.class, value=PrestashopBooleanAdapter.class)
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
