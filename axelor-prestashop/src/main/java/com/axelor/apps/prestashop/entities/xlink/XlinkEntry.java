package com.axelor.apps.prestashop.entities.xlink;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.axelor.apps.prestashop.entities.PrestashopResourceType;

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

	public String getHref() {
		return href;
	}

	public boolean isRead() {
		return read;
	}

	public boolean isCreate() {
		return create;
	}

	public boolean isUpdate() {
		return update;
	}

	public boolean isDelete() {
		return delete;
	}

	public boolean isHead() {
		return head;
	}

	public abstract PrestashopResourceType getEntryType();

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
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.ADDRESSES;
		}
	}

	@XmlRootElement(name="carts")
	public static class CartsXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.CARTS;
		}
	}

	@XmlRootElement(name="categories")
	public static class CategoriesXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.CATEGORIES;
		}
	}

	@XmlRootElement(name="countries")
	public static class CountriesXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.COUNTRIES;
		}
	}

	@XmlRootElement(name="currencies")
	public static class CurrenciesXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.CURRENCIES;
		}
	}

	@XmlRootElement(name="customers")
	public static class CustomersXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.CUSTOMERS;
		}
	}

	@XmlRootElement(name="images")
	public static class ImagesXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.IMAGES;
		}
	}

	@XmlRootElement(name="languages")
	public static class LanguagesXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.LANGUAGES;
		}
	}

	@XmlRootElement(name="order_details")
	public static class OrderDetailsXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.ORDER_DETAILS;
		}
	}

	@XmlRootElement(name="order_histories")
	public static class OrderHistoriesXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.ORDER_HISTORIES;
		}
	}

	@XmlRootElement(name="orders")
	public static class OrdersXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.ORDERS;
		}
	}

	@XmlRootElement(name="products")
	public static class ProductsXlink extends XlinkEntry {
		@Override
		public PrestashopResourceType getEntryType() {
			return PrestashopResourceType.PRODUCTS;
		}
	}
}
