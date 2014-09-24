/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import java.math.BigDecimal;
import java.util.HashMap;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class CurrencyConversionLineController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
	@Inject
	private CurrencyConversionService ccs;
	
	@Inject
	private CurrencyConversionLineRepository cclRepo;
	
	@Inject
	private GeneralService gs;
	
	@Inject
	private CurrencyRepository currencyRepo;
	
	
	public void checkDate(ActionRequest request, ActionResponse response) {
	
		CurrencyConversionLine ccl = request.getContext().asType(CurrencyConversionLine.class);
		
		LOG.debug("Currency Conversion Line Id : {}",ccl.getId());

		if (ccl.getId() != null && cclRepo.all().filter("self.startCurrency.id = ?1 and self.endCurrency.id = ?2 and (self.toDate = null OR  self.toDate >= ?3) and self.id != ?4)",ccl.getStartCurrency().getId(),ccl.getEndCurrency().getId(),ccl.getFromDate(),ccl.getId()).count() > 0) {
			response.setFlash("ATTENTION : Veuillez clôturer la période actuelle de conversion pour en créer une nouvelle.");
//			response.setValue("fromDate", "");
		}
		else if(ccl.getId() == null && cclRepo.all().filter("self.startCurrency.id = ?1 and self.endCurrency.id = ?2 and (self.toDate = null OR  self.toDate >= ?3))",ccl.getStartCurrency().getId(),ccl.getEndCurrency().getId(),ccl.getFromDate()).count() > 0) {
			response.setFlash("ATTENTION : Veuillez clôturer la période actuelle de conversion pour en créer une nouvelle.");
//			response.setValue("fromDate", "");
		}
		
		if(ccl.getFromDate() != null && ccl.getToDate() != null && ccl.getFromDate().isAfter(ccl.getToDate())){
			response.setFlash("La date de fin doit impérativement être égale ou supérieur à la date de début.");
//			response.setValue("fromDate", "");
		}
	
	}
	
	public void applyExchangeRate(ActionRequest request, ActionResponse response) {
		Context context = request.getContext();
		
		LOG.debug("Apply Conversion Rate Context: {}",new Object[]{context});
		
		HashMap currencyFrom = (HashMap)context.get("startCurrency");
		HashMap currencyTo = (HashMap)context.get("endCurrency");
		
		if(currencyFrom.get("id") != null && currencyTo.get("id") != null){
			Currency fromCurrency = currencyRepo.find(Long.parseLong(currencyFrom.get("id").toString()));
			Currency toCurrency  = currencyRepo.find(Long.parseLong(currencyTo.get("id").toString()));
			LocalDate today = gs.getTodayDate();
			CurrencyConversionLine cclCoverd = cclRepo.all().filter("startCurrency = ?1 AND endCurrency = ?2 AND fromDate >= ?3 AND (toDate <= ?3 OR toDate = null)",fromCurrency,toCurrency,today).fetchOne();
			
			if(cclCoverd == null){
				CurrencyConversionLine ccl = cclRepo.all().filter("startCurrency = ?1 AND endCurrency = ?2",fromCurrency,toCurrency).order("-fromDate").fetchOne();
				LOG.debug("Last conversion line {}",ccl);
				
				if(ccl != null && ccl.getToDate() == null){
					ccl.setToDate(today.minusDays(1));
					ccs.saveCurrencyConversionLine(ccl);
				}
				
				BigDecimal rate = new BigDecimal(context.get("newExchangeRate").toString());
				String variation = null;
			    if(ccl != null)
			    	variation = ccs.getVariations(rate, ccl.getExchangeRate());
			    General general = null;
			    
			    if(context.get("general") != null && ((HashMap)context.get("general")).get("id") != null)
			    	general = gs.find(Long.parseLong(((HashMap)context.get("general")).get("id").toString()));
			    
				ccs.createCurrencyConversionLine(fromCurrency,toCurrency,today,rate,general,variation);
				
			}
			else
				response.setFlash("ATTENTION : Veuillez clôturer la période actuelle de conversion pour en créer une nouvelle");
		}
		else
			response.setFlash("Both currencies must be saved before currency rate apply");
		LOG.debug("Set can close for wizard");
		response.setCanClose(true);
	}
	
	public void convert(ActionRequest request, ActionResponse response) {
		Context context = request.getContext();
		Currency fromCurrency = (Currency)context.get("startCurrency");
		Currency toCurrency =  (Currency)context.get("endCurrency");
		CurrencyConversionLine prevLine = null;
		
		if(fromCurrency.getId() != null && toCurrency.getId() != null){
			
			if(context.get("id") != null)
				prevLine = cclRepo.all().filter("startCurrency.id = ?1 AND endCurrency.id = ?2 AND id != ?3",fromCurrency.getId(),toCurrency.getId(),context.get("id")).order("-fromDate").fetchOne();
			else
				prevLine = cclRepo.all().filter("startCurrency.id = ?1 AND endCurrency.id = ?2",fromCurrency.getId(),toCurrency.getId()).order("-fromDate").fetchOne();

			LOG.debug("Previous currency conversion line: {}",prevLine);
			fromCurrency = currencyRepo.find(fromCurrency.getId());
			toCurrency  = currencyRepo.find(toCurrency.getId());
			BigDecimal rate = ccs.convert(fromCurrency, toCurrency);
			
			if(rate.compareTo(new BigDecimal(-1)) == 0)
				response.setFlash("Currency conversion webservice not working");
			else {
				response.setValue("variations", "0");
				if(context.get("_model").equals("com.axelor.apps.base.db.Wizard"))
					response.setValue("newExchangeRate", rate);
				else
					response.setValue("exchangeRate", rate);
				response.setValue("fromDate", gs.getTodayDate());
				if(prevLine != null)
					response.setValue("variations", ccs.getVariations(rate, prevLine.getExchangeRate()));
			}
			
		}
	}
	
}
