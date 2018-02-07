package com.axelor.apps.prestashop.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Element;

public class Associations {
	private ImagesAssociationsEntry images;
	private List<Element> additionalEntries;

	public ImagesAssociationsEntry getImages() {
		return images;
	}

	public void setImages(ImagesAssociationsEntry images) {
		this.images = images;
	}

	@XmlAnyElement
	public List<Element> getAdditionalEntries() {
		return additionalEntries;
	}

	public void setAdditionalEntries(List<Element> additionalEntries) {
		this.additionalEntries = additionalEntries;
	}

	public static abstract class AssociationsEntry {
		@XmlAttribute
		protected String nodeType;
		@XmlAttribute
		protected String api;

		// Just for JAXB to stop complain
		@SuppressWarnings("unused")
		private AssociationsEntry() {}

		public AssociationsEntry(String nodeType, String api) {
			this.nodeType = nodeType;
			this.api = api;
		}
	}

	public static class ImagesAssociationsEntry extends AssociationsEntry {
		private List<ImagesAssociationElement> images = new LinkedList<>();

		public ImagesAssociationsEntry() {
			super("image", "images");
		}

		@XmlElement(name="image")
		public List<ImagesAssociationElement> getImages() {
			return images;
		}

		public void setImages(List<ImagesAssociationElement> images) {
			this.images = images;
		}
	}

	public static class ImagesAssociationElement {
		private String href;
		private Integer id;

		@XmlAttribute(name="href", namespace="http://www.w3.org/1999/xlink")
		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
	}
}
