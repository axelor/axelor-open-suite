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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.prestashop.entities.PrestashopCountry;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.entities.PrestashopTranslatableString.PrestashopTranslationEntry;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportCountryServiceImpl implements ExportCountryService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private CountryRepository countryRepo;

	@Inject
	public ExportCountryServiceImpl(CountryRepository countryRepo) {
		this.countryRepo = countryRepo;
	}

	@Override
	@Transactional
	public void exportCountry(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter logBuffer) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logBuffer.write(String.format("%n====== COUNTRIES ======%n"));

		List<Country> countries;
		if(endDate == null) {
			countries = countryRepo.all().fetch();
		} else {
			countries = countryRepo.all().filter("self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId = null", endDate, endDate).fetch();
		}

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());

		// Same as usual, perform a global fetch to speed up process
		final List<PrestashopCountry> remoteCountries = ws.fetchAll(PrestashopResourceType.COUNTRIES);
		final Map<Integer, PrestashopCountry> countriesById = new HashMap<>();
		final Map<String, PrestashopCountry> countriesByCode = new HashMap<>();
		for(PrestashopCountry country : remoteCountries) {
			countriesById.put(country.getId(), country);
			countriesByCode.put(country.getIsoCode(), country);
		}

		final PrestashopCountry defaultCountry = ws.fetchDefault(PrestashopResourceType.COUNTRIES);


		for(Country localCountry : countries) {
			logBuffer.write(String.format("Exporting country #%d (%s) – ", localCountry.getId(), localCountry.getName()));

			try {
				PrestashopCountry remoteCountry;
				if(localCountry.getPrestaShopId() != null) {
					logBuffer.write("prestashop id=" + localCountry.getPrestaShopId());
					remoteCountry = countriesById.get(localCountry.getPrestaShopId());
					if(remoteCountry == null) {
						logBuffer.write(String.format(" [ERROR] Not found remotely%n"));
						log.error("Unable to fetch remote country #{} ({}), something's probably very wrong, skipping",
								localCountry.getPrestaShopId(), localCountry.getName());
						++errors;
						continue;
					} else if(localCountry.getAlpha2Code().equals(remoteCountry.getIsoCode()) == false) {
						log.error("Remote country #{} has not the same ISO code as the local one ({} vs {}), skipping",
								localCountry.getPrestaShopId(), remoteCountry.getIsoCode(), localCountry.getAlpha2Code());
						logBuffer.write(String.format(" [ERROR] ISO code mismatch: %s vs %s%n", remoteCountry.getIsoCode(), localCountry.getAlpha2Code()));
						++errors;
						continue;
					}
				} else {
					remoteCountry = countriesByCode.get(localCountry.getAlpha2Code());
					if(remoteCountry == null) {
						logBuffer.write("no ID and code not found, creating");
						remoteCountry = new PrestashopCountry();
						remoteCountry.setIsoCode(localCountry.getAlpha2Code());
						remoteCountry.setName(defaultCountry.getName().clone());
						for(PrestashopTranslationEntry e : remoteCountry.getName().getTranslations()) {
							e.setTranslation(localCountry.getName());
						}
					} else {
						logBuffer.write(String.format("found remotely using its code %s", localCountry.getAlpha2Code()));
					}
				}

				Integer phonePrefix = null;
				if(StringUtils.isNotEmpty(localCountry.getPhonePrefix())) {
					String localPhonePrefix = localCountry.getPhonePrefix().replaceAll("[^0-9]", "");
					if(StringUtils.isNotEmpty(localPhonePrefix)) {
						phonePrefix = Integer.parseInt(localPhonePrefix);
					}
				}
				remoteCountry.setCallPrefix(phonePrefix);
				// FIXME handle language correctly, only override value for appConfig.textsLanguage
				if(remoteCountry.getName().getTranslations().size() > 0) {
					remoteCountry.getName().getTranslations().get(0).setTranslation(localCountry.getName());
				}

				remoteCountry = ws.save(PrestashopResourceType.COUNTRIES, remoteCountry);
				localCountry.setPrestaShopId(remoteCountry.getId());
				logBuffer.write(String.format(" [SUCCESS]%n"));
				++done;
			} catch (PrestaShopWebserviceException e) {
				logBuffer.write(String.format(" [ERROR] %s (full trace is in application logs)%n", e.getLocalizedMessage()));
				log.error(String.format("Exception while synchronizing country #%d (%s)", localCountry.getId(), localCountry.getAlpha2Code()), e);
				++errors;
			}
		}

		logBuffer.write(String.format("%n=== END OF COUNTRIES IMPORT, done: %d, errors: %d ===%n", done, errors));
	}

}
