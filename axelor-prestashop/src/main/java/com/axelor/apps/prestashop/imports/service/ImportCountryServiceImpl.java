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
package com.axelor.apps.prestashop.imports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportCountryServiceImpl implements ImportCountryService {

	PSWebServiceClient ws;
    HashMap<String,Object> opt;
    JSONObject schema;
    private final String shopUrl;
	private final String key;
	
	@Inject
	private CountryRepository countryRepo;
	
	/**
	 * Initialization
	 */
	public ImportCountryServiceImpl() {
			AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
			shopUrl = prestaShopObj.getPrestaShopUrl();
			key = prestaShopObj.getPrestaShopKey();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importCountry(BufferedWriter bwImport)
			throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {
		
		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Country");
		
		ws = new PSWebServiceClient(shopUrl,key);
		List<Integer> countryIds = ws.fetchApiIds("countries");
		
		for (Integer id : countryIds) {
			
			ws = new PSWebServiceClient(shopUrl,key);
			opt = new HashMap<String, Object>();
			opt.put("resource", "countries");
			opt.put("id", id);
			schema = ws.getJson(opt);
			
			try {
				
				JSONArray names = schema.getJSONObject("country").getJSONArray("name");
				JSONObject childJSONObject = names.getJSONObject(0);
				
				if(childJSONObject.getString("value").isEmpty()) {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COUNTRY), IException.NO_VALUE);
				}
						
				Country country = Beans.get(CountryRepository.class).all().filter("self.alpha2Code = ?", schema.getJSONObject("country").getString("iso_code")).fetchOne();
				if(country == null) {
					country = new Country();
				}
				country.setName(childJSONObject.getString("value"));
				country.setAlpha2Code(schema.getJSONObject("country").getString("iso_code"));
				country.setPrestaShopId(String.valueOf(schema.getJSONObject("country").getInt("id")));
				countryRepo.save(country);
				done++;
						
			} catch (AxelorException e) {
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {
						
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}
		
		bwImport.newLine();
		bwImport.newLine();
		bwImport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
		return bwImport;
	}
}
