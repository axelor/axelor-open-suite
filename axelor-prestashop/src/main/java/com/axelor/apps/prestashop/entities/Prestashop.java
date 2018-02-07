package com.axelor.apps.prestashop.entities;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Element;

/**
 * Common root tag for all API calls. Interesting content
 * is in content attribute, which should be casted based
 * on the kind of request.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Prestashop {
	@XmlElementRef
	private PrestashopContainerEntity content;
	// Some structures (eg. images) have an additional content element
	// avoid parsing issues
	@XmlAnyElement
	private List<Element> additionalAttributes;

	@SuppressWarnings("unchecked")
	public <T extends PrestashopContainerEntity> T getContent() {
		return (T)content;
	}

	public void setContent(PrestashopContainerEntity content) {
		this.content = content;
	}

	public List<Element> getAdditionalAttributes() {
		return additionalAttributes;
	}

	public void setAdditionalAttributes(List<Element> additionalAttributes) {
		this.additionalAttributes = additionalAttributes;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("content", content)
				.toString();
	}
}
