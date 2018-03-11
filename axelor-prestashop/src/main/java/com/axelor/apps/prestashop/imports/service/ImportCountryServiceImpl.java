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
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.prestashop.entities.PrestashopCountry;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportCountryServiceImpl implements ImportCountryService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private CountryRepository countryRepo;

	@Inject
	public ImportCountryServiceImpl(CountryRepository countryRepo) {
		this.countryRepo = countryRepo;
	}

	@Override
	@Transactional
	public void importCountry(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter logBuffer) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logBuffer.write(String.format("%n====== COUNTRIES ======%n"));

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
		List<PrestashopCountry> remoteCountries = ws.fetchAll(PrestashopResourceType.COUNTRIES);

		for(PrestashopCountry remoteCountry : remoteCountries) {
			logBuffer.write(String.format("Importing country #%d (%s) – ", remoteCountry.getId(), remoteCountry.getName().getTranslation(1)));

			Country localCountry = countryRepo.findByPrestaShopId(remoteCountry.getId());
			if(localCountry == null) {
				localCountry = countryRepo.findByAlpha2Code(remoteCountry.getIsoCode());
				if(localCountry== null) {
					logBuffer.write("not found by ID and code not found, creating");
					localCountry = new Country();
					localCountry.setAlpha2Code(remoteCountry.getIsoCode());
					localCountry.setPrestaShopId(remoteCountry.getId());
				} else {
					logBuffer.write(String.format("found locally using its code %s", localCountry.getAlpha2Code()));
				}
			} else {
				if(localCountry.getAlpha2Code().equals(remoteCountry.getIsoCode()) == false) {
					log.error("Remote country #{} has not the same ISO code as the local one ({} vs {}), skipping",
							remoteCountry.getId(), remoteCountry.getIsoCode(), localCountry.getAlpha2Code());
					logBuffer.write(String.format(" [ERROR] ISO code mismatch: %s vs %s%n", remoteCountry.getIsoCode(), localCountry.getAlpha2Code()));
					++errors;
					continue;
				}
			}

			// As the field is prestashop specific, always update it
			localCountry.setPrestaShopZoneId(remoteCountry.getZoneId());

			if(localCountry.getId() == null || appConfig.getPrestaShopMasterForCountries() == Boolean.TRUE) {
				localCountry.setName(remoteCountry.getName().getTranslation(1)); // TODO Handle language correctly
				if(remoteCountry.getCallPrefix() != null) {
					localCountry.setPhonePrefix(remoteCountry.getCallPrefix().toString());
				}
				countryRepo.save(localCountry);
			} else {
				logBuffer.write(" – local country exists and PrestaShop isn't master for countries, leaving untouched");
			}
			logBuffer.write(String.format(" [SUCCESS]%n"));
			++done;
		}

		logBuffer.write(String.format("%n=== END OF COUNTRIES IMPORT, done: %d, errors: %d ===%n", done, errors));
	}
}
