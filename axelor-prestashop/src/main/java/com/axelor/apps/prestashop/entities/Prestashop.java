package com.axelor.apps.prestashop.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

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

	@SuppressWarnings("unchecked")
	public <T extends PrestashopContainerEntity> T getContent() {
		return (T)content;
	}

	public void setContent(PrestashopContainerEntity content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("content", content)
				.toString();
	}
}
