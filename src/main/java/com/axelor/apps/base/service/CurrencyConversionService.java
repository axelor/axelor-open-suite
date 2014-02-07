/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service;

import groovy.util.Proxy;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

import org.hamcrest.core.IsInstanceOf;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.http.HTTPClient;
import wslite.http.HTTPMethod;
import wslite.http.HTTPRequest;
import wslite.http.HTTPResponse;

public class CurrencyConversionService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyConversionService.class);
	
	@Inject
	private TraceBackService tb;
	
	@Inject
	private GeneralService gs;
	 

	public BigDecimal convert(Currency currencyFrom, Currency currencyTo){
		BigDecimal rate = null; 
		
		LOG.debug("Currerncy conversion From: {} To: {}",new Object[] { currencyFrom,currencyTo});
		String wsUrl = gs.getGeneral().getCurrencyWsURL();
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
		        Float rt = Float.parseFloat(response.getContentAsString());
		        rate = BigDecimal.valueOf(rt).setScale(4,RoundingMode.HALF_EVEN);
			}catch(Exception e){
				e.printStackTrace();
				tb.trace(e);
			}
		}
		
		else
			LOG.info("Currency from and to must be filled to get rate");
		LOG.debug("Currerncy conversion rate: {}",new Object[] {rate});
		return rate;
	}
	
	public String getVariations(BigDecimal currentRate, BigDecimal previousRate){
		String variations = null;
		LOG.debug("Currency rate variation calculation for CurrentRate: {} PreviousRate: {}", new Object[]{currentRate,previousRate});
		
		if(currentRate != null && previousRate != null && previousRate.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal diffRate = currentRate.subtract(previousRate);
			BigDecimal variation = diffRate.multiply(new BigDecimal(100)).divide(previousRate,RoundingMode.HALF_EVEN);
			variation = variation.setScale(2,RoundingMode.HALF_EVEN);
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
		ccl.setConversionRate(rate);
		ccl.setGeneral(general);
		ccl.setVariations(variations);
		ccl.save();
		
	}
	
	@Transactional 
	public void saveCurrencyConversionLine(CurrencyConversionLine ccl){
		ccl.save();
	}
	
	
	public BigDecimal getRate(Currency currencyFrom, Currency currencyTo, LocalDate rateDate){
		
		LOG.debug("Get Last rate for CurrencyFrom: {} CurrencyTo: {} RateDate: {}",new Object[]{currencyFrom,currencyTo,rateDate});
		
		BigDecimal rate = null;
		
		if(currencyFrom != null && currencyTo != null && rateDate != null){
			currencyFrom = Currency.find(currencyFrom.getId());
			currencyTo = Currency.find(currencyTo.getId());
			CurrencyConversionLine ccl = CurrencyConversionLine.all().filter("startCurrency = ?1 AND endCurrency = ?2 AND fromDate <= ?3 AND (toDate >= ?3 OR toDate = null)",currencyFrom,currencyTo,rateDate).fetchOne();
			if(ccl != null)
				rate =  ccl.getConversionRate();
		}
		
		LOG.debug("Current Rate: {}",new Object[]{rate});
		
		return rate;
	}
}
