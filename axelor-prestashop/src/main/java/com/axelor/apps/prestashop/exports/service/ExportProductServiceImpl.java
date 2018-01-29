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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.prestashop.db.Categories;
import com.axelor.apps.prestashop.db.Language;
import com.axelor.apps.prestashop.db.LanguageDetails;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.db.Products;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ExportProductServiceImpl implements ExportProductService {

	Integer done = 0;
	Integer anomaly = 0;
	private final String shopUrl;
	private final String key;

	@Inject
	private ProductRepository productRepo;

	@Inject
	private ProductCategoryRepository productCategoryRepo;

	/**
	 * Initialization
	 */
	public ExportProductServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public void exportProduct(ZonedDateTime endDate, BufferedWriter bwExport)
			throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException,
			SAXException, PrestaShopWebserviceException, JAXBException, TransformerFactoryConfigurationError {

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Product");
		List<Product> products = null;
		String schema = null;
		Document document;
		PSWebServiceClient ws = null;
		HashMap<String, Object> opt = null;
		String prestaShopId = null;

		if(endDate == null) {
			products = Beans.get(ProductRepository.class).all().filter("self.sellable = true").fetch();
		} else {
			products = Beans.get(ProductRepository.class).all().filter("self.sellable = true AND (self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId = null)", endDate, endDate).fetch();
		}


		for (Product productObj : products) {

			try {
				Products product = new Products();
				product.setId(productObj.getPrestaShopId());

				if (!productObj.getName().equals("")) {

					ProductCategory productCategory = productCategoryRepo.findByName(productObj.getProductCategory().getName());
					String prestaShopCategoryId = productCategory.getPrestaShopId().toString();
					product.setId_category_default(prestaShopCategoryId);

					Categories categories = new Categories();
					categories.setId(prestaShopCategoryId);
					product.setCategories(categories);

					product.setPrice(productObj.getSalePrice().setScale(0, RoundingMode.HALF_UP).toString());
					product.setWidth(productObj.getWidth().toString());
					product.setMinimal_quantity("2");
					product.setOn_sale("0");
					product.setActive("1");
					product.setAvailable_for_order("1");
					product.setShow_price("1");
					product.setState("1");

					LanguageDetails nameDetails = new LanguageDetails();
					nameDetails.setId("1");
					nameDetails.setValue(productObj.getName());
					Language nameLanguage = new Language();
					nameLanguage.setLanguage(nameDetails);
					product.setName(nameLanguage);

					LanguageDetails descriptionDetails = new LanguageDetails();
					descriptionDetails.setId("1");
					descriptionDetails.setValue(productObj.getDescription());
					Language descriptionLanguage = new Language();
					descriptionLanguage.setLanguage(descriptionDetails);
					product.setDescription(descriptionLanguage);

					LanguageDetails linkRewriteDetails = new LanguageDetails();
					linkRewriteDetails.setId("1");
					linkRewriteDetails.setValue(productObj.getProductTypeSelect().toString());
					Language linkRewriteLanguage = new Language();
					linkRewriteLanguage.setLanguage(linkRewriteDetails);
					product.setLink_rewrite(linkRewriteLanguage);

					Prestashop prestaShop = new Prestashop();
					prestaShop.setPrestashop(product);

					StringWriter sw = new StringWriter();
					JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
					Marshaller marshallerObj = contextObj.createMarshaller();
					marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshallerObj.marshal(prestaShop, sw);
					schema = sw.toString();

					ws = new PSWebServiceClient(shopUrl + "/api/" + "products" + "?schema=synopsis", key);
					opt = new HashMap<String, Object>();
					opt.put("resource", "products");
					opt.put("postXml", schema);

					if (productObj.getPrestaShopId() == null) {
						document = ws.add(opt);
						prestaShopId = document.getElementsByTagName("id").item(0).getTextContent();
						productObj.setPrestaShopId(prestaShopId);

						if (productObj.getPicture() != null) {
							Path path = MetaFiles.getPath(productObj.getPicture());
							ws = new PSWebServiceClient(shopUrl, key);
							ws.addImg(path.toUri().toString(), Integer.parseInt(prestaShopId));
						}

						NodeList nodeList = document.getElementsByTagName("stock_availables").item(0).getChildNodes();
						String stock_id = null;
						for (int i = 0; i < nodeList.getLength(); i++) {
							if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
								Element element = (Element) nodeList.item(i);
								stock_id = element.getElementsByTagName("id").item(0).getTextContent().toString();
							}
						}

						List<StockMoveLine> moveLine = Beans.get(StockMoveLineRepository.class).all().filter("self.stockMove.statusSelect = 3 and (self.stockMove.fromStockLocation.typeSelect = 1 or self.stockMove.toStockLocation.typeSelect = 1) and self.product = ?", productObj).fetch();
						BigDecimal totalRealQty = BigDecimal.ZERO;

						if(!moveLine.isEmpty()) {

							for(int i=0 ; i<moveLine.size(); i++) {
								totalRealQty =  totalRealQty.add(moveLine.get(i).getRealQty());
							}

							ws = new PSWebServiceClient(shopUrl, key);
							opt = new HashMap<String, Object>();
							opt.put("resource", "stock_availables");
							opt.put("id", stock_id);
							Document stockSchema = ws.get(opt);
							stockSchema.getElementsByTagName("quantity").item(0).setTextContent(totalRealQty.setScale(0, RoundingMode.HALF_UP).toString());
							opt.put("postXml", ws.DocumentToString(stockSchema));
							ws.edit(opt);

						}

					} else {
						opt.put("id", productObj.getPrestaShopId());
						ws = new PSWebServiceClient(shopUrl, key);
						document = ws.edit(opt);
					}

					productRepo.save(productObj);
					done++;

				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_PRODUCT), IException.NO_VALUE);
				}

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + productObj.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + productObj.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
}
