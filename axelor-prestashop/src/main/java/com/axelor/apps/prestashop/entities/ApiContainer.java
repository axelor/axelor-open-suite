package com.axelor.apps.prestashop.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Element;

import com.axelor.apps.prestashop.entities.xlink.XlinkEntry;

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
