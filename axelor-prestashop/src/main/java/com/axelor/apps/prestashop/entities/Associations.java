package com.axelor.apps.prestashop.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Element;

public class Associations {
	private ImagesAssociationsEntry images;
	private AvailableStocksAssociationsEntry availableStocks;
	private List<Element> additionalEntries;

	public ImagesAssociationsEntry getImages() {
		return images;
	}

	public void setImages(ImagesAssociationsEntry images) {
		this.images = images;
	}

	public AvailableStocksAssociationsEntry getAvailableStocks() {
		return availableStocks;
	}

	@XmlElement(name="stock_availables")
	public void setAvailableStocks(AvailableStocksAssociationsEntry availableStocks) {
		this.availableStocks = availableStocks;
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

	public static class AvailableStocksAssociationsEntry extends AssociationsEntry {
		private List<AvailableStocksAssociationElement> stocks = new LinkedList<>();

		public AvailableStocksAssociationsEntry() {
			super("stock_available", "stock_availables");
		}

		@XmlElement(name="stock_available")
		public List<AvailableStocksAssociationElement> getStock() {
			return stocks;
		}

		public void setStock(List<AvailableStocksAssociationElement> stocks) {
			this.stocks = stocks;
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

	public static class AvailableStocksAssociationElement {
		private String href;
		private Integer id;
		private Integer productAttributeId;

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

		@XmlElement(name="id_product_attribute")
		public Integer getProductAttributeId() {
			return productAttributeId;
		}

		public void setProductAttributeId(Integer productAttributeId) {
			this.productAttributeId = productAttributeId;
		}
	}
}
