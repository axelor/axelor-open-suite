package com.axelor.apps.prestashop;

import java.io.ByteArrayInputStream;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.axelor.apps.prestashop.entities.Api;
import com.axelor.apps.prestashop.entities.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.entities.xlink.XlinkEntry;
import com.google.common.collect.Sets;

public class UnmarshallTest {

	@Test
	public void test() throws JAXBException {
		final String xml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<prestashop xmlns:xlink=\"http://www.w3.org/1999/xlink\">\r\n" +
				"<api shopName=\"Diel\">\r\n" +
				"<addresses xlink:href=\"http://192.168.1.23/api/addresses\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/addresses\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The Customer, Brand and Customer addresses</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/addresses?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/addresses?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</addresses>\r\n" +
				"<carts xlink:href=\"http://192.168.1.23/api/carts\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/carts\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"Customer's carts</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/carts?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/carts?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</carts>\r\n" +
				"<categories xlink:href=\"http://192.168.1.23/api/categories\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/categories\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The product categories</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/categories?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/categories?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</categories>\r\n" +
				"<countries xlink:href=\"http://192.168.1.23/api/countries\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/countries\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The countries</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/countries?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/countries?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</countries>\r\n" +
				"<customers xlink:href=\"http://192.168.1.23/api/customers\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/customers\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The e-shop's customers</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/customers?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/customers?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</customers>\r\n" +
				"<images xlink:href=\"http://192.168.1.23/api/images\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/images\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The images</description>\r\n" +
				"</images>\r\n" +
				"<languages xlink:href=\"http://192.168.1.23/api/languages\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/languages\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"Shop languages</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/languages?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/languages?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</languages>\r\n" +
				"<order_details xlink:href=\"http://192.168.1.23/api/order_details\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/order_details\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"Details of an order</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/order_details?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/order_details?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</order_details>\r\n" +
				"<order_histories xlink:href=\"http://192.168.1.23/api/order_histories\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/order_histories\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The Order histories</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/order_histories?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/order_histories?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</order_histories>\r\n" +
				"<orders xlink:href=\"http://192.168.1.23/api/orders\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/orders\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The Customers orders</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/orders?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/orders?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</orders>\r\n" +
				"<products xlink:href=\"http://192.168.1.23/api/products\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"<description xlink:href=\"http://192.168.1.23/api/products\" get=\"true\" put=\"true\" post=\"true\" delete=\"true\" head=\"true\">\r\n" +
				"The products</description>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/products?schema=blank\" type=\"blank\"/>\r\n" +
				"<schema xlink:href=\"http://192.168.1.23/api/products?schema=synopsis\" type=\"synopsis\"/>\r\n" +
				"</products>\r\n" +
				"</api>\r\n" +
				"</prestashop>\r\n";
		Prestashop envelop = (Prestashop) JAXBContext.newInstance("com.axelor.apps.prestashop.entities:com.axelor.apps.prestashop.entities.xlink").createUnmarshaller().unmarshal(new ByteArrayInputStream(xml.getBytes()));

		final Set<PrestashopResourceType> expectedEntries = Sets.newHashSet(
				PrestashopResourceType.ADDRESSES,
				PrestashopResourceType.CARTS,
				PrestashopResourceType.CATEGORIES,
				PrestashopResourceType.COUNTRIES,
				PrestashopResourceType.CUSTOMERS,
				PrestashopResourceType.IMAGES,
				PrestashopResourceType.LANGUAGES,
				PrestashopResourceType.ORDER_DETAILS,
				PrestashopResourceType.ORDER_HISTORIES,
				PrestashopResourceType.ORDERS,
				PrestashopResourceType.PRODUCTS
		);

		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(Api.class, envelop.getContent().getClass());
		Api content = envelop.getContent();
		Assert.assertEquals(expectedEntries.size(), content.getXlinkEntries().size());
		for(XlinkEntry entry : content.getXlinkEntries()) {
			Assert.assertTrue(expectedEntries.remove(entry.getEntryType()));
		}
		Assert.assertEquals(0, expectedEntries.size());
	}

}
