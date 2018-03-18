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
package com.axelor.apps.prestashop.entities.xlink;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Element;

import com.axelor.apps.prestashop.entities.PrestashopContainerEntity;

@XmlRootElement(name="api")
public class ApiContainer extends PrestashopContainerEntity {
	@XmlAttribute
	private String shopName;

	@XmlElementRef
	private List<XlinkEntry> xlinkEntries = new LinkedList<>();

	@XmlAnyElement
	private List<Element> unknownEntries = new LinkedList<>();

	public List<XlinkEntry> getXlinkEntries() {
		return xlinkEntries;
	}

	public List<Element> getUnknownEntries() {
		return unknownEntries;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("shopName", shopName)
				.append("xlinkEntries", xlinkEntries)
				.toString();
	}
}
