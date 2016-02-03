/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wslite.http.HTTPClient;
import wslite.http.HTTPMethod;
import wslite.http.HTTPRequest;
import wslite.http.HTTPResponse;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CurrencyConversionService {

	private static final Logger LOG = LoggerFactory.getLogger(CurrencyConversionService.class);

	@Inject
	private CurrencyRepository currencyRepo;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	private CurrencyConversionLineRepository cclRepo;


	public BigDecimal convert(Currency currencyFrom, Currency currencyTo){
		BigDecimal rate = new BigDecimal(-1);

		LOG.debug("Currerncy conversion From: {} To: {}",new Object[] { currencyFrom,currencyTo});
		String wsUrl = generalService.getGeneral().getCurrencyWsURL();
		if(wsUrl == null){
			LOG.info("Currency WS URL not configured");
			return rate;
		}

		if(currencyFrom != null && currencyTo != null){
			try{
		        HTTPClient httpclient = new HTTPClient();
		        HTTPRequest request = new HTTPRequest();
		        URL url = new URL(String.format(wsUrl,currencyFrom.getCode(),currencyTo.getCode()));
		        LOG.debug("Currency conversion webservice URL: {}" ,new Object[]{url.toString()});
		        request.setUrl(url);
		        request.setMethod(HTTPMethod.GET);
		        HTTPResponse response = httpclient.execute(request);
		        LOG.debug("Webservice response code: {}, reponse mesasage: {}",response.getStatusCode(),response.getStatusMessage());
		        if(response.getStatusCode() != 200)
		        	return rate;
		        Float rt = Float.parseFloat(response.getContentAsString());
		        rate = BigDecimal.valueOf(rt).setScale(4,RoundingMode.HALF_EVEN);
			} catch (Exception e) {
				TraceBackService.trace(e);
				e.printStackTrace();
			}
		}
		else
			LOG.info("Currency from and to must be filled to get rate");
		LOG.debug("Currerncy conversion rate: {}",new Object[] {rate});
		return rate;
	}

	public String getVariations(BigDecimal currentRate, BigDecimal previousRate){
		String variations = "0";
		LOG.debug("Currency rate variation calculation for CurrentRate: {} PreviousRate: {}", new Object[]{currentRate,previousRate});

		if(currentRate != null && previousRate != null && previousRate.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal diffRate = currentRate.subtract(previousRate);
			BigDecimal variation = diffRate.multiply(new BigDecimal(100)).divide(previousRate,RoundingMode.HALF_EVEN);
			variation = variation.setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);
			variations = variation.toString()+"%";
		}

		LOG.debug("Currency rate variation result: {}",new Object[]{variations});
		return variations;
	}

	@Transactional
	public void createCurrencyConversionLine(Currency currencyFrom, Currency currencyTo, LocalDate fromDate, BigDecimal rate, General general, String variations){
		LOG.debug("Create new currency conversion line CurrencyFrom: {}, CurrencyTo: {},FromDate: {},ConversionRate: {}, General: {}, Variations: {}",
				   new Object[]{currencyFrom,currencyTo,fromDate,rate,general,variations});

		CurrencyConversionLine ccl = new CurrencyConversionLine();
		ccl.setStartCurrency(currencyFrom);
		ccl.setEndCurrency(currencyTo);
		ccl.setFromDate(fromDate);
		ccl.setExchangeRate(rate);
		ccl.setGeneral(general);
		ccl.setVariations(variations);
		cclRepo.save(ccl);

	}

	@Transactional
	public void saveCurrencyConversionLine(CurrencyConversionLine ccl){
		cclRepo.save(ccl);
	}


	public BigDecimal getRate(Currency currencyFrom, Currency currencyTo, LocalDateTime rateDate){

		LOG.debug("Get Last rate for CurrencyFrom: {} CurrencyTo: {} RateDate: {}",new Object[]{currencyFrom,currencyTo,rateDate});

		BigDecimal rate = null;

		if(currencyFrom != null && currencyTo != null && rateDate != null){
			currencyFrom = currencyRepo.find(currencyFrom.getId());
			currencyTo = currencyRepo.find(currencyTo.getId());
			CurrencyConversionLine ccl = cclRepo.all().filter("startCurrency = ?1 AND endCurrency = ?2 AND fromDate <= ?3 AND (toDate >= ?3 OR toDate = null)",currencyFrom,currencyTo,rateDate).fetchOne();
			if(ccl != null)
				rate =  ccl.getExchangeRate();
		}

		LOG.debug("Current Rate: {}",new Object[]{rate});

		return rate;
	}
	
}
