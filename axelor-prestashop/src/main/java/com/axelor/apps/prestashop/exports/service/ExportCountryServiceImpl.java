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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.prestashop.db.Countries;
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

public class ExportCountryServiceImpl implements ExportCountryService {

	Integer done = 0;
	Integer anomaly = 0;
	private final String shopUrl;
	private final String key;

	@Inject
	private CountryRepository countryRepo;

	/**
	 * Initialization
	 */
	public ExportCountryServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}

	/**
	 *  Check on prestashop country is already there
	 *
	 * @param countryCode unique code of country
	 * @return id of prestashop's country if it is.
	 * @throws PrestaShopWebserviceException
	 */
	public String countryExists(AppPrestashop appConfig, String countryCode) throws PrestaShopWebserviceException {
		String prestaShopId = null;
		PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
		HashMap<String, String> countryMap = new HashMap<String, String>();
		countryMap.put("iso_code", countryCode);
		HashMap<String, Object> opt = new HashMap<String, Object>();
		opt.put("resource", "countries");
		opt.put("filter", countryMap);
		Document str =  ws.get(opt);

		NodeList list = str.getElementsByTagName("countries");
		for(int i = 0; i < list.getLength(); i++) {
		    Element element = (Element) list.item(i);
		    NodeList node = element.getElementsByTagName("country");
		    Node country = node.item(i);
		    if(node.getLength() > 0) {
		    	prestaShopId = country.getAttributes().getNamedItem("id").getNodeValue();
		    	return prestaShopId;
		    }
		}
		return prestaShopId;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public void exportCountry(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter bwExport) throws IOException, PrestaShopWebserviceException, ParserConfigurationException, SAXException, TransformerException {

		PSWebServiceClient ws = null;
		HashMap<String, Object> opt = null;
		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Country");
		List<Country> countries = null;
		Document document = null;
		String schema = null;
		String prestaShopId = null;

		if(endDate == null) {
			countries = Beans.get(CountryRepository.class).all().fetch();
		} else {
			countries = Beans.get(CountryRepository.class).all().filter("self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId = null", endDate, endDate).fetch();
		}

		for(Country countryObj : countries) {
			try {

				prestaShopId = this.isCountry(countryObj.getAlpha2Code());

				if(countryObj.getName() == null) {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COUNTRY), IException.NO_VALUE);
				}

				LanguageDetails languageObj = new LanguageDetails();
				languageObj.setId("1");
				languageObj.setValue(countryObj.getName());

				Language language = new Language();
				language.setLanguage(languageObj);

				Countries country = new Countries();
				if(prestaShopId != null) {
					country.setId(prestaShopId);
				} else {
					country.setId(countryObj.getPrestaShopId());
				}

				country.setName(language);
				country.setIso_code(countryObj.getAlpha2Code());
				country.setId_zone("1");
				country.setContains_states("0");
				country.setNeed_identification_number("0");
				country.setDisplay_tax_label("1");
				country.setActive("1");
				Prestashop prestaShop = new Prestashop();
				prestaShop.setPrestashop(country);

				StringWriter sw = new StringWriter();
				JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
				Marshaller marshallerObj = contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshallerObj.marshal(prestaShop, sw);
				schema = sw.toString();

				ws = new PSWebServiceClient(shopUrl + "/api/" + "countries" + "?schema=synopsis", key);
				opt = new HashMap<String, Object>();
				opt.put("resource", "countries");
				opt.put("postXml", schema);

				if (countryObj.getPrestaShopId() == null && prestaShopId == null) {
					document = ws.add(opt);

				} else if (prestaShopId != null){
					opt.put("id", prestaShopId);
					ws = new PSWebServiceClient(shopUrl, key);
					document = ws.edit(opt);

				} else {
					opt.put("id", countryObj.getPrestaShopId());
					ws = new PSWebServiceClient(shopUrl, key);
					document = ws.edit(opt);
				}

				countryObj.setPrestaShopId(document.getElementsByTagName("id").item(0).getTextContent());
				countryRepo.save(countryObj);
				done++;

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + countryObj.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {

				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + countryObj.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
}
