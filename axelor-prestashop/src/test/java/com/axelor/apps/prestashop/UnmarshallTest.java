package com.axelor.apps.prestashop;

import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.axelor.apps.prestashop.entities.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.entities.xlink.ApiContainer;
import com.axelor.apps.prestashop.entities.xlink.XlinkEntry;
import com.google.common.collect.Sets;

public class UnmarshallTest {

	@Test
	public void testApi() throws JAXBException {
		Prestashop envelop = (Prestashop)JAXBContext.newInstance("com.axelor.apps.prestashop.entities:com.axelor.apps.prestashop.entities.xlink")
				.createUnmarshaller()
				.unmarshal(getClass().getResourceAsStream("api.xml"));

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
		Assert.assertEquals(ApiContainer.class, envelop.getContent().getClass());
		ApiContainer content = envelop.getContent();
		Assert.assertEquals(expectedEntries.size(), content.getXlinkEntries().size());
		for(XlinkEntry entry : content.getXlinkEntries()) {
			Assert.assertTrue(expectedEntries.remove(entry.getEntryType()));
		}
		Assert.assertEquals(0, expectedEntries.size());
	}

}
