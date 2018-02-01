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
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.prestashop.db.Currencies;
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

public class ExportCurrencyServiceImpl implements ExportCurrencyService {

	Integer done = 0;
	Integer anomaly = 0;
	private final String shopUrl;
	private final String key;

	@Inject
	CurrencyRepository currencyRepo;

	/**
	 * Initialization
	 */
	public ExportCurrencyServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}

	/**
	 * Check on prestashop currency is already there
	 *
	 * @param currencyCode unique code of currency
	 * @return id of prestashop's currency if there it is.
	 * @throws PrestaShopWebserviceException
	 */
	public String currencyExists(AppPrestashop appConfig, String currencyCode) throws PrestaShopWebserviceException {

		String prestaShopId = null;
		PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
		HashMap<String, String> currencyMap = new HashMap<String, String>();
		currencyMap.put("iso_code", currencyCode);
		HashMap<String, Object> opt = new HashMap<String, Object>();
		opt.put("resource", "currencies");
		opt.put("filter", currencyMap);
		Document str =  ws.get(opt);

		NodeList list = str.getElementsByTagName("currencies");
		for(int i = 0; i < list.getLength(); i++) {
		    Element element = (Element) list.item(i);
		    NodeList node = element.getElementsByTagName("currency");
		    Node currency = node.item(i);
		    if(node.getLength() > 0) {
		    	prestaShopId = currency.getAttributes().getNamedItem("id").getNodeValue();
		    	return prestaShopId;
		    }
		}
		return prestaShopId;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public void exportCurrency(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter bwExport) throws IOException, TransformerException, ParserConfigurationException, SAXException, PrestaShopWebserviceException, JAXBException, TransformerFactoryConfigurationError {

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Currency");
		List<Currency> currencies = null;
		String prestaShopId = null;
		String schema = null;
		Document document = null;

		if(endDate == null) {
			currencies = Beans.get(CurrencyRepository.class).all().fetch();
		} else {
			currencies = Beans.get(CurrencyRepository.class).all().filter("self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId = null", endDate, endDate).fetch();
		}


		for (Currency currencyObj : currencies) {
			try {

				if(currencyObj.getCode() == null && currencyObj.getName() == null) {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CURRENCY),IException.NO_VALUE);
				}

				Currencies currency = new Currencies();
				currency.setId(currencyObj.getPrestaShopId());
				currency.setName(currencyObj.getName());
				currency.setIso_code(currencyObj.getCode());
				currency.setConversion_rate("1.00");
				currency.setDeleted("0");
				currency.setActive("1");
				Prestashop prestaShop = new Prestashop();
				prestaShop.setPrestashop(currency);

				StringWriter sw = new StringWriter();
				JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
				Marshaller marshallerObj = contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshallerObj.marshal(prestaShop, sw);
				schema = sw.toString();

				PSWebServiceClient ws = new PSWebServiceClient(shopUrl + "/api/" + "currencies" + "?schema=synopsis", key);
				HashMap<String, Object> opt = new HashMap<String, Object>();
				opt.put("resource", "currencies");
				opt.put("postXml", schema);

				if(currencyObj.getPrestaShopId() == null) {
					document = ws.add(opt);
				} else {
					opt.put("id", currencyObj.getPrestaShopId());
					ws = new PSWebServiceClient(shopUrl, key);
					document = ws.edit(opt);
				}

				currencyObj.setPrestaShopId(document.getElementsByTagName("id").item(0).getTextContent());
				currencyRepo.save(currencyObj);
				done++;

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + currencyObj.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {

				String errorXml = e.getMessage();
				String errorCode = null;
				errorXml = errorXml.substring(errorXml.indexOf('\n')+1);
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(errorXml));
				Document str = db.parse(is);

				NodeList list = str.getElementsByTagName("errors");
				for(int i = 0; i < list.getLength(); i++) {
					if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {

						Element element = (Element) list.item(i);
						errorCode = element.getElementsByTagName("code").item(i).getTextContent();
					}
				}

				if(errorCode.equals("46")) {
					prestaShopId = this.isCurrency(currencyObj.getCode());
					currencyObj.setPrestaShopId(prestaShopId);
					currencyRepo.save(currencyObj);
					done++;
					continue;
				}
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + currencyObj.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
}
