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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

@XmlRootElement(name="country")
public class PrestashopCountry extends PrestashopIdentifiableEntity {
	private PrestashopTranslatableString name;
	private int zoneId;
	private Integer currencyId;
	private Integer callPrefix;
	private String isoCode;
	private boolean active = true;
	private boolean containsStates = false;
	private boolean needsIdentificationNumber = false;
	private boolean needsZipcode = false;
	private String zipCodeFormat;
	private boolean displayTaxLabel = true;
	private List<Element> additionalProperties = new LinkedList<>();

	public PrestashopTranslatableString getName() {
		return name;
	}

	public void setName(PrestashopTranslatableString name) {
		this.name = name;
	}

	@XmlElement(name="id_zone")
	public int getZoneId() {
		return zoneId;
	}

	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
	}

	@XmlElement(name="id_currency")
	public Integer getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(Integer currencyId) {
		this.currencyId = currencyId;
	}

	@XmlElement(name="call_prefix")
	public Integer getCallPrefix() {
		return callPrefix;
	}

	public void setCallPrefix(Integer callPrefix) {
		this.callPrefix = callPrefix;
	}

	@XmlElement(name="iso_code")
	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@XmlElement(name="contains_states")
	public boolean isContainsStates() {
		return containsStates;
	}

	public void setContainsStates(boolean containsStates) {
		this.containsStates = containsStates;
	}

	@XmlElement(name="need_identification_number")
	public boolean isNeedsIdentificationNumber() {
		return needsIdentificationNumber;
	}

	public void setNeedsIdentificationNumber(boolean needsIdentificationNumber) {
		this.needsIdentificationNumber = needsIdentificationNumber;
	}

	@XmlElement(name="need_zip_code")
	public boolean isNeedsZipcode() {
		return needsZipcode;
	}

	public void setNeedsZipcode(boolean needsZipcode) {
		this.needsZipcode = needsZipcode;
	}

	@XmlElement(name="zip_code_format")
	public String getZipCodeFormat() {
		return zipCodeFormat;
	}

	public void setZipCodeFormat(String zipCodeFormat) {
		this.zipCodeFormat = zipCodeFormat;
	}

	@XmlElement(name="display_tax_label")
	public boolean isDisplayTaxLabel() {
		return displayTaxLabel;
	}

	public void setDisplayTaxLabel(boolean displayTaxLabel) {
		this.displayTaxLabel = displayTaxLabel;
	}

	@XmlAnyElement
	public List<Element> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(List<Element> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
}
