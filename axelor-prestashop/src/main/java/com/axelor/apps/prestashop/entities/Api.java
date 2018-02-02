package com.axelor.apps.prestashop.entities;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.axelor.apps.prestashop.entities.xlink.XlinkEntry;

@XmlRootElement(name="api")
public class Api extends PrestashopContainerEntity {
	@XmlAttribute
	private String shopName;

	@XmlElementRef
	private List<XlinkEntry> xlinkEntries;

	public List<XlinkEntry> getXlinkEntries() {
		return xlinkEntries;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("shopName", shopName)
				.append("xlinkEntries", xlinkEntries)
				.toString();
	}
}
