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

@XmlRootElement(name="language")
public class PrestashopLanguage extends PrestashopIdentifiableEntity {
	private String name;
	private String isoCode;
	private String locale;
	private String languageCode;
	private Boolean active;
	private Boolean rtl;
	private String liteDateFormat;
	private String fullDateFormat;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="iso_code")
	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@XmlElement(name="language_code")
	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@XmlElement(name="is_rtl")
	public Boolean getRtl() {
		return rtl;
	}

	public void setRtl(Boolean rtl) {
		this.rtl = rtl;
	}

	@XmlElement(name="date_format_lite")
	public String getLiteDateFormat() {
		return liteDateFormat;
	}

	public void setLiteDateFormat(String liteDateFormat) {
		this.liteDateFormat = liteDateFormat;
	}

	@XmlElement(name="date_format_full")
	public String getFullDateFormat() {
		return fullDateFormat;
	}

	public void setFullDateFormat(String fullDateFormat) {
		this.fullDateFormat = fullDateFormat;
	}
}
