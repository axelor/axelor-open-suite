/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.prestashop.exports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.prestashop.db.Categories;
import com.axelor.apps.prestashop.db.Language;
import com.axelor.apps.prestashop.db.LanguageDetails;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportCategoryServiceImpl implements ExportCategoryService {
	private ProductCategoryRepository categoryRepo;

	@Inject
	public ExportCategoryServiceImpl(ProductCategoryRepository categoryRepo) {
		this.categoryRepo = categoryRepo;
	}

	@Override
	@Transactional
	public void exportCategory(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter bwExport) throws IOException,
			PrestaShopWebserviceException, ParserConfigurationException, SAXException, TransformerException {
		int done = 0;
		int anomaly = 0;

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Category");
		List<ProductCategory> categories = null;
		String schema = null;
		Document document = null;

		if(endDate == null) {
			categories = categoryRepo.all().fetch();
		} else {
			categories = categoryRepo.all().filter("self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId IS NULL", endDate, endDate).fetch();
		}

		for (ProductCategory productCategory : categories) {

			try {

				Categories category = new Categories();
				category.setId(Objects.toString(productCategory.getPrestaShopId(), null));
				category.setActive("1");

				if (productCategory.getPrestaShopId() != null) {
					if(productCategory.getPrestaShopId() == 1 || productCategory.getPrestaShopId() == 2) {
						continue;
					}
				}

				if (!productCategory.getName().equals("") && !productCategory.getCode().equals("")) {

					if (productCategory.getParentProductCategory() == null || productCategory.getParentProductCategory().getPrestaShopId() == 1) {
						category.setId_parent("2");
					} else {
						category.setId_parent(Objects.toString(productCategory.getParentProductCategory().getPrestaShopId(), null));
					}

					LanguageDetails nameDetails = new LanguageDetails();
					nameDetails.setId("1");
					nameDetails.setValue(productCategory.getName());
					Language nameLanguage = new Language();
					nameLanguage .setLanguage(nameDetails);
					category.setName(nameLanguage);

					LanguageDetails linkRewriteDetails = new LanguageDetails();
					linkRewriteDetails.setId("1");
					linkRewriteDetails.setValue(productCategory.getCode());
					Language linkRewriteLanguage = new Language();
					linkRewriteLanguage.setLanguage(linkRewriteDetails);
					category.setLink_rewrite(linkRewriteLanguage);

					Prestashop prestaShop = new Prestashop();
					prestaShop.setPrestashop(category);

					StringWriter sw = new StringWriter();
					JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
					Marshaller marshallerObj = contextObj.createMarshaller();
					marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshallerObj.marshal(prestaShop, sw);
					schema = sw.toString();

					PSWebServiceClient ws;

					Options options = new Options();
					options.setResourceType(PrestashopResourceType.CATEGORIES);
					options.setXmlPayload(schema);

					if (productCategory.getPrestaShopId() == null) {
						ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/categories?schema=synopsis", appConfig.getPrestaShopKey());
						document = ws.add(options);
					} else {
						ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
						options.setRequestedId(productCategory.getPrestaShopId());
						document = ws.edit(options);
					}

					productCategory.setPrestaShopId(Integer.valueOf(document.getElementsByTagName("id").item(0).getTextContent()));
					categoryRepo.save(productCategory);
					done++;

				} else {
					throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_PRODUCT_CATEGORY));
				}

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + productCategory.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + productCategory.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
}
