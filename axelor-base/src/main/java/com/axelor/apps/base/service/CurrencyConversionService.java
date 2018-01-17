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
package com.axelor.apps.base.service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wslite.http.HTTPClient;
import wslite.http.HTTPMethod;
import wslite.http.HTTPRequest;
import wslite.http.HTTPResponse;
import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CurrencyConversionService {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	protected AppBaseService appBaseService;
	
	@Inject
	private CurrencyConversionLineRepository cclRepo;


	public BigDecimal convert(Currency currencyFrom, Currency currencyTo){
		BigDecimal rate = new BigDecimal(-1);
		
		LOG.debug("Currerncy conversion From: {} To: {}",new Object[] { currencyFrom,currencyTo});
		String wsUrl = appBaseService.getAppBase().getCurrencyWsURL();
		if(wsUrl == null){
			LOG.info("Currency WS URL not configured");
			return rate;
		}

		if(currencyFrom != null && currencyTo != null){
			try{
		        HTTPClient httpclient = new HTTPClient();
		        HTTPRequest request = new HTTPRequest();
		        Map<String, Object> headers = new HashMap<>();
				headers.put("Accept", "application/json");
				request.setHeaders(headers);
		        URL url = new URL(String.format(wsUrl,currencyFrom.getCode(),currencyTo.getCode(), LocalDate.now().minus(Period.ofDays(1))));
		        LOG.debug("Currency conversion webservice URL: {}" ,new Object[]{url.toString()});
		        request.setUrl(url);
		        request.setMethod(HTTPMethod.GET);
		        HTTPResponse response = httpclient.execute(request);
		        LOG.debug("Webservice response code: {}, reponse mesasage: {}",response.getStatusCode(),response.getStatusMessage());
		        if(response.getStatusCode() != 200)
		        	return rate;

		        Float rt = this.getRateFromJson(currencyFrom, currencyTo, response);
		        rate = BigDecimal.valueOf(rt).setScale(8, RoundingMode.HALF_EVEN);
		        
			} catch (Exception e) {
				TraceBackService.trace(e);
			}
		}
		else
			LOG.info("Currency from and to must be filled to get rate");
		LOG.debug("Currerncy conversion rate: {}",new Object[] {rate});
		return rate;
	}
	
	private Float getRateFromJson(Currency currencyFrom, Currency currencyTo, HTTPResponse response) throws JSONException {
		int compareCode = currencyFrom.getCode().compareTo(currencyTo.getCode());
        Float rt = null;
        String[] currencyRateArr = new String[2];
        
        JSONObject jsonResult = new JSONObject(response.getContentAsString());
        JSONObject dataSets = new JSONObject(jsonResult.getJSONArray("dataSets").get(0).toString());
        JSONObject series = new JSONObject(dataSets.getJSONObject("series").toString());
        JSONObject seriesOf = null;
        JSONObject observations = null;
        JSONArray rateValue = null;
        		
        if (series.size() > 1) {
        	for (int i = 0; i < series.size(); i++) {
        		seriesOf = new JSONObject(series.getJSONObject("0:"+i+":0:0:0").toString());
		        observations = new JSONObject(seriesOf.getJSONObject("observations").toString());
		        rateValue = new JSONArray(observations.get(observations.length() - 1).toString());
		        currencyRateArr[i] = rateValue.get(0).toString();
        	}
        	if (compareCode > 0) {
        		rt = Float.parseFloat(currencyRateArr[0]) / Float.parseFloat(currencyRateArr[1]);
        	} else {
        		rt = Float.parseFloat(currencyRateArr[1]) / Float.parseFloat(currencyRateArr[0]);
        	}
        	
        } else {
        	seriesOf = new JSONObject(series.getJSONObject("0:0:0:0:0").toString());
	        observations = new JSONObject(seriesOf.getJSONObject("observations").toString());
	        rateValue = new JSONArray(observations.get(observations.length() - 1).toString());
	        
        	if (currencyTo.getCode().equals("EUR")) {
        		rt = 1.0f / Float.parseFloat(rateValue.get(0).toString());
        	} else {
        		rt = Float.parseFloat(rateValue.get(0).toString());
        	}
        }
        return rt;
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
	public void createCurrencyConversionLine(Currency currencyFrom, Currency currencyTo, LocalDate fromDate, BigDecimal rate, AppBase appBase, String variations){
		LOG.debug("Create new currency conversion line CurrencyFrom: {}, CurrencyTo: {},FromDate: {},ConversionRate: {}, AppBase: {}, Variations: {}",
				   new Object[]{currencyFrom,currencyTo,fromDate,rate,appBase,variations});

		CurrencyConversionLine ccl = new CurrencyConversionLine();
		ccl.setStartCurrency(currencyFrom);
		ccl.setEndCurrency(currencyTo);
		ccl.setFromDate(fromDate);
		ccl.setExchangeRate(rate);
		ccl.setAppBase(appBase);
		ccl.setVariations(variations);
		cclRepo.save(ccl);

	}

	@Transactional
	public void saveCurrencyConversionLine(CurrencyConversionLine ccl){
		cclRepo.save(ccl);
	}
	
}
