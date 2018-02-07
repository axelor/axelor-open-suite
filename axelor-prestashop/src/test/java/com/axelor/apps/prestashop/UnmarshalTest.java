package com.axelor.apps.prestashop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.junit.Assert;
import org.junit.Test;

import com.axelor.apps.prestashop.entities.ListContainer.CountriesContainer;
import com.axelor.apps.prestashop.entities.ListContainer.CurrenciesContainer;
import com.axelor.apps.prestashop.entities.ListContainer.ProductCategoriesContainer;
import com.axelor.apps.prestashop.entities.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopCountry;
import com.axelor.apps.prestashop.entities.PrestashopCurrency;
import com.axelor.apps.prestashop.entities.PrestashopImage;
import com.axelor.apps.prestashop.entities.PrestashopProductCategory;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.entities.xlink.ApiContainer;
import com.axelor.apps.prestashop.entities.xlink.XlinkEntry;
import com.google.common.collect.Sets;

public class UnmarshalTest {

	@Test
	public void testApi() throws JAXBException {
		Prestashop envelop = (Prestashop)JAXBContext.newInstance("com.axelor.apps.prestashop.entities:com.axelor.apps.prestashop.entities.xlink")
				.createUnmarshaller()
				.unmarshal(getClass().getResourceAsStream("api.xml"));

		final Set<PrestashopResourceType> expectedEntries = Sets.newHashSet(
				PrestashopResourceType.ADDRESSES,
				PrestashopResourceType.CARTS,
				PrestashopResourceType.PRODUCT_CATEGORIES,
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

	@Test
	public void testCurrency() throws JAXBException {
		Prestashop envelop = (Prestashop) JAXBContext.newInstance("com.axelor.apps.prestashop.entities")
				.createUnmarshaller()
				.unmarshal(getClass().getResourceAsStream("currency.xml"));

		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(PrestashopCurrency.class, envelop.getContent().getClass());
		PrestashopCurrency currency = envelop.getContent();
		Assert.assertEquals(Integer.valueOf(1), currency.getId());
		Assert.assertEquals("euro", currency.getName());
		Assert.assertEquals("EUR", currency.getCode());
		Assert.assertEquals(new BigDecimal("1.000000"), currency.getConversionRate());
		Assert.assertEquals(false, currency.isDeleted());
		Assert.assertEquals(true, currency.isActive());
	}

	@Test
	public void testCurrencies() throws JAXBException {
		Prestashop envelop = (Prestashop) JAXBContext.newInstance("com.axelor.apps.prestashop.entities")
				.createUnmarshaller()
				.unmarshal(getClass().getResourceAsStream("currencies.xml"));

		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(CurrenciesContainer.class, envelop.getContent().getClass());
		CurrenciesContainer currencies = envelop.getContent();
		Assert.assertEquals(166, currencies.getEntities().size());
	}

	@Test
	public void testCountry() throws JAXBException {
		Prestashop envelop = (Prestashop) JAXBContext.newInstance("com.axelor.apps.prestashop.entities")
				.createUnmarshaller()
				.unmarshal(getClass().getResourceAsStream("country.xml"));


		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(PrestashopCountry.class, envelop.getContent().getClass());
		PrestashopCountry country = envelop.getContent();
		Assert.assertNotNull(country.getName());
		Assert.assertEquals(1, country.getName().getTranslations().size());
		Assert.assertEquals("ALLEMAGNE", country.getName().getTranslations().get(0).getTranslation());
		Assert.assertEquals(Integer.valueOf(1), country.getId());
		Assert.assertEquals(1, country.getZoneId());
		Assert.assertEquals(Integer.valueOf(0), country.getCurrencyId());
	}

	@Test
	public void testCountries() throws JAXBException {
		Prestashop envelop = (Prestashop) JAXBContext.newInstance("com.axelor.apps.prestashop.entities")
				.createUnmarshaller()
				.unmarshal(getClass().getResourceAsStream("countries.xml"));

		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(CountriesContainer.class, envelop.getContent().getClass());
		CountriesContainer countries = envelop.getContent();
		Assert.assertEquals(251	, countries.getEntities().size());
	}

	@Test
	public void testProductCategory() throws JAXBException {
		Unmarshaller um = JAXBContext.newInstance("com.axelor.apps.prestashop.entities").createUnmarshaller();
		um.setEventHandler(new DefaultValidationEventHandler());
		Prestashop envelop = (Prestashop)um.unmarshal(getClass().getResourceAsStream("product-category.xml"));


		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(PrestashopProductCategory.class, envelop.getContent().getClass());
		PrestashopProductCategory category = envelop.getContent();
		Assert.assertEquals(Integer.valueOf(1), category.getId());
		Assert.assertEquals(Integer.valueOf(0), category.getParentId());
		Assert.assertEquals(true, category.isActive());
		Assert.assertEquals(Integer.valueOf(1), category.getDefaultShopId());
		Assert.assertEquals(false, category.isRootCategory());
		Assert.assertEquals(Integer.valueOf(0), category.getPosition());
		Assert.assertEquals(LocalDateTime.of(2018, 1, 29, 14, 8, 27), category.getAddDate());
		Assert.assertEquals(LocalDateTime.of(2018, 1, 29, 14, 8, 27), category.getUpdateDate());
		Assert.assertEquals(2, category.getName().getTranslations().size());
		Assert.assertEquals("Racine", category.getName().getTranslation(1));
		Assert.assertEquals("Root", category.getName().getTranslation(2));
		Assert.assertEquals(1, category.getAdditionalProperties().size());
		Assert.assertEquals("associations", category.getAdditionalProperties().get(0).getTagName());
	}

	@Test
	public void testProductCategories() throws JAXBException {
		Prestashop envelop = (Prestashop) JAXBContext.newInstance("com.axelor.apps.prestashop.entities")
				.createUnmarshaller()
				.unmarshal(getClass().getResourceAsStream("product-categories.xml"));

		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(ProductCategoriesContainer.class, envelop.getContent().getClass());
		ProductCategoriesContainer categories = envelop.getContent();
		Assert.assertEquals(20, categories.getEntities().size());
	}

	@Test
	public void testImage() throws JAXBException {
		Unmarshaller um = JAXBContext.newInstance("com.axelor.apps.prestashop.entities").createUnmarshaller();
		um.setEventHandler(new DefaultValidationEventHandler());
		Prestashop envelop = (Prestashop)um.unmarshal(getClass().getResourceAsStream("image.xml"));

		Assert.assertNotNull(envelop.getContent());
		Assert.assertEquals(PrestashopImage.class, envelop.getContent().getClass());
		PrestashopImage image = envelop.getContent();
		Assert.assertEquals(Integer.valueOf(29), image.getId());
		Assert.assertEquals(Integer.valueOf(57), image.getProductId());
		Assert.assertEquals(Integer.valueOf(1), image.getPosition());
		Assert.assertEquals(true, image.isCover());
		Assert.assertNotNull(image.getLegend());
		Assert.assertEquals("Une jolie image", image.getLegend().getTranslation(1));
		Assert.assertEquals("A nice picture", image.getLegend().getTranslation(2));
	}
}
