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

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.prestashop.entities.PrestashopCurrency;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ImportCurrencyServiceImpl implements ImportCurrencyService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private CurrencyRepository currencyRepo;
	private AppBaseService appBaseService;
	private CurrencyService currencyService;
	private CurrencyConversionService currencyConversionService;

	@Inject
	public ImportCurrencyServiceImpl(CurrencyRepository currencyRepo, AppBaseService appBaseService,
			CurrencyService currencyService, CurrencyConversionService currencyConversionService) {
		this.currencyRepo = currencyRepo;
		this.appBaseService = appBaseService;
		this.currencyService = currencyService;
		this.currencyConversionService = currencyConversionService;
	}



	@Override
	@Transactional
	public void importCurrency(AppPrestashop appConfig, ZonedDateTime endDate, Writer logBuffer) throws IOException, PrestaShopWebserviceException {
		Integer done = 0;
		Integer errors = 0;

		logBuffer.write(String.format("%n====== CURRENCIES ======%n"));

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
		// When endDate is not null, we could add a filter for date_add, date_upd (PS supports >=), but as
		// we've no way of knowing which currencies have already been imported, it would imply that we must
		// never miss a run
		final List<PrestashopCurrency> remoteCurrencies = ws.fetchAll(PrestashopResourceType.CURRENCIES);

		for(PrestashopCurrency remoteCurrency : remoteCurrencies) {
			logBuffer.write("Importing currency " + remoteCurrency.getCode() + " – ");
			Currency localCurrency = currencyRepo.findByPrestaShopId(remoteCurrency.getId());
			if(localCurrency == null) {
				localCurrency = currencyRepo.findByCode(remoteCurrency.getCode());
				if(localCurrency == null) {
					logBuffer.write("no ID and code not found, creating");
					localCurrency = new Currency();
					localCurrency.setCode(remoteCurrency.getCode());
					localCurrency.setPrestaShopId(remoteCurrency.getId());
				} else {
					logBuffer.write(String.format("found locally using its code %s", localCurrency.getCode()));
				}
			} else {
				if(localCurrency.getCode().equals(remoteCurrency.getCode()) == false) {
					log.error("Remote currency #{} has not the same ISO code as the local one ({} vs {}), skipping",
							localCurrency.getPrestaShopId(), remoteCurrency.getCode(), localCurrency.getCode());
					logBuffer.write(String.format(" [ERROR] ISO code mismatch: %s vs %s%n", remoteCurrency.getCode(), localCurrency.getCode()));
					++errors;
					continue;
				}
			}

			if(appConfig.getPrestaShopMasterForCurrencies() || localCurrency.getId() == null) {
				localCurrency.setName(remoteCurrency.getName());
				currencyRepo.save(localCurrency);
				BigDecimal currentRate;
				try {
					currentRate = currencyService.getCurrencyConversionRate(localCurrency, appConfig.getPrestaShopCurrency(), LocalDate.now());
				} catch(AxelorException ae) {
					// Would be far simpler if getCurrencyConversionRate was just returning null…
					currentRate = BigDecimal.ONE;
				}
				if(remoteCurrency.getConversionRate() != null &&
						BigDecimal.ZERO.compareTo(remoteCurrency.getConversionRate()) != 0 &&
						BigDecimal.ONE.compareTo(remoteCurrency.getConversionRate()) != 0 &&
						currentRate.compareTo(remoteCurrency.getConversionRate()) != 0) {
					currencyConversionService.createCurrencyConversionLine(localCurrency, appConfig.getPrestaShopCurrency(),
							LocalDate.now(), remoteCurrency.getConversionRate(),
							appBaseService.getAppBase(), currencyConversionService.getVariations(remoteCurrency.getConversionRate(), currentRate));
				}
			} else {
				logBuffer.write(" – local currency exists and PrestaShop isn't master for currencies, leaving untouched");
			}
			logBuffer.write(String.format(" [SUCCESS]%n"));
			++done;
		}

		logBuffer.write(String.format("%n=== END OF CURRENCIES IMPORT, done: %d, errors: %d ===%n", done, errors));
	}
}
