package com.axelor.apps.prestashop.entities.xlink;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class XlinkEntry {
	@XmlAttribute(namespace="http://www.w3.org/1999/xlink", name="href")
	private String href;

	@XmlAttribute(name="get")
	private boolean read;

	@XmlAttribute(name="post")
	private boolean create;

	@XmlAttribute(name="put")
	private boolean update;

	@XmlAttribute
	private boolean delete;

	@XmlAttribute
	private boolean head;

	public static enum XlinkEntryType {
		ADDRESSES,
		CARTS,
		CATEGORIES,
		COUNTRIES,
		CURRENCIES,
		CUSTOMERS,
		IMAGES,
		LANGUAGES,
		ORDER_DETAILS,
		ORDER_HISTORIES,
		ORDERS,
		PRODUCTS
	}

	public abstract XlinkEntryType getEntryType();

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("href", href)
				.append("create", create)
				.append("read", read)
				.append("update", update)
				.append("delete", delete)
				.append("head", head)
				.toString();
	}

	@XmlRootElement(name="addresses")
	public static class AddressesXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.ADDRESSES;
		}
	}

	@XmlRootElement(name="carts")
	public static class CartsXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.CARTS;
		}
	}

	@XmlRootElement(name="categories")
	public static class CategoriesXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.CATEGORIES;
		}
	}

	@XmlRootElement(name="countries")
	public static class CountriesXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.COUNTRIES;
		}
	}

	@XmlRootElement(name="currencies")
	public static class CurrenciesXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.CURRENCIES;
		}
	}

	@XmlRootElement(name="customers")
	public static class CustomersXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.CUSTOMERS;
		}
	}

	@XmlRootElement(name="images")
	public static class ImagesXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.IMAGES;
		}
	}

	@XmlRootElement(name="languages")
	public static class LanguagesXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.LANGUAGES;
		}
	}

	@XmlRootElement(name="order_details")
	public static class OrderDetailsXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.ORDER_DETAILS;
		}
	}

	@XmlRootElement(name="order_histories")
	public static class OrderHistoriesXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.ORDER_HISTORIES;
		}
	}

	@XmlRootElement(name="orders")
	public static class OrdersXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.ORDERS;
		}
	}

	@XmlRootElement(name="products")
	public static class ProductsXlink extends XlinkEntry {
		@Override
		public XlinkEntryType getEntryType() {
			return XlinkEntryType.PRODUCTS;
		}
	}
}
