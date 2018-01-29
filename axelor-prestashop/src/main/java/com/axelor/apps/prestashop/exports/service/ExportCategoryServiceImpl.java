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
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.prestashop.db.Categories;
import com.axelor.apps.prestashop.db.Language;
import com.axelor.apps.prestashop.db.LanguageDetails;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ExportCategoryServiceImpl implements ExportCategoryService {

	Integer done = 0;
	Integer anomaly = 0;
	private final String shopUrl;
	private final String key;

	@Inject
	private ProductCategoryRepository categoryRepo;

	/**
	 * Initialization
	 */
	public ExportCategoryServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public void exportCategory(ZonedDateTime endDate, BufferedWriter bwExport) throws IOException,
			PrestaShopWebserviceException, ParserConfigurationException, SAXException, TransformerException {

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Category");
		List<ProductCategory> categories = null;
		String schema = null;
		Document document = null;

		if(endDate == null) {
			categories = Beans.get(ProductCategoryRepository.class).all().fetch();
		} else {
			categories = Beans.get(ProductCategoryRepository.class).all().filter("self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId = null", endDate, endDate).fetch();
		}

		for (ProductCategory productCategory : categories) {

			try {

				Categories category = new Categories();
				category.setId(productCategory.getPrestaShopId());
				category.setActive("1");

				if (productCategory.getPrestaShopId() != null) {
					if(productCategory.getPrestaShopId().equals("1") || productCategory.getPrestaShopId().equals("2")) {
						continue;
					}
				}

				if (!productCategory.getName().equals("") && !productCategory.getCode().equals("")) {

					if (productCategory.getParentProductCategory() == null || productCategory.getParentProductCategory().getPrestaShopId().equals("1") || productCategory.getParentProductCategory().getPrestaShopId().equals("1")) {
						category.setId_parent("2");
					} else {
						category.setId_parent(productCategory.getParentProductCategory().getPrestaShopId());
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

					PSWebServiceClient ws = new PSWebServiceClient(shopUrl + "/api/" + "categories" + "?schema=synopsis", key);
					HashMap<String, Object> opt = new HashMap<String, Object>();
					opt.put("resource", "categories");
					opt.put("postXml", schema);

					if (productCategory.getPrestaShopId() == null) {
						document = ws.add(opt);
					} else {
						opt.put("id", productCategory.getPrestaShopId());
						ws = new PSWebServiceClient(shopUrl, key);
						document = ws.edit(opt);
					}

					productCategory.setPrestaShopId(document.getElementsByTagName("id").item(0).getTextContent());
					categoryRepo.save(productCategory);
					done++;

				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_PRODUCT_CATEGORY),	IException.NO_VALUE);
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
