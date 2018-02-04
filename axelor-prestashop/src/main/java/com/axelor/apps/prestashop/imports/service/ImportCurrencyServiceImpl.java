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
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportCurrencyServiceImpl implements ImportCurrencyService {

    private final String shopUrl;
	private final String key;

	@Inject
	private CurrencyRepository currencyRepo;

	/**
	 * Initialization
	 */
	public ImportCurrencyServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importCurrency(BufferedWriter bwImport) throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {

		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Currency");

		PSWebServiceClient ws = new PSWebServiceClient(shopUrl,key);
		List<Integer> currencyIds = ws.fetchApiIds(PrestashopResourceType.CURRENCIES);

		for (Integer id : currencyIds) {

			try {
				// FIXME just us display on global fetch instead of having hundreds of individual requests
				Options options = new Options();
				options.setResourceType(PrestashopResourceType.CURRENCIES);
				options.setRequestedId(id);
				JSONObject schema = ws.getJson(options);

				Currency currency = null;
				currency = Beans.get(CurrencyRepository.class).all().filter("self.prestaShopId = ?", id).fetchOne();

				if(currency == null) {
					currency = currencyRepo.findByCode(schema.getJSONObject("currency").getString("iso_code"));
					if(currency == null) {
						currency = new Currency();
					}
					currency.setPrestaShopId(schema.getJSONObject("currency").getInt("id"));
				}

				if(!schema.getJSONObject("currency").getString("iso_code").equals(null) &&
						!schema.getJSONObject("currency").getString("name").equals(null)) {

					currency.setCode(schema.getJSONObject("currency").getString("iso_code"));
					currency.setName(schema.getJSONObject("currency").getString("name"));
				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CURRENCY), IException.NO_VALUE);
				}

				currencyRepo.save(currency);
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
