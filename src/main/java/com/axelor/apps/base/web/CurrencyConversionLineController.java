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
package com.axelor.apps.base.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.meta.service.MetaTranslations;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class CurrencyConversionLineController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
	@Inject
	CurrencyConversionService ccs;
	
	@Inject
	GeneralService gs;
	
	@Inject
	private MetaTranslations metaTranslations;
	
	public void checkDate(ActionRequest request, ActionResponse response) {
	
		CurrencyConversionLine ccl = request.getContext().asType(CurrencyConversionLine.class);

		if (ccl.getId() != null && CurrencyConversionLine.all().filter("self.startCurrency.id = ?1 and self.endCurrency.id = ?2 and (self.toDate = null OR  self.toDate >= ?3) and self.id != ?4)",ccl.getStartCurrency().getId(),ccl.getEndCurrency().getId(),ccl.getFromDate(),ccl.getId()).count() > 0) {
			response.setFlash("ATTENTION : Veuillez clôturer la période actuelle de conversion pour en créer une nouvelle.");
			response.setValue("fromDate", "");
		}
		else if(ccl.getId() == null && CurrencyConversionLine.all().filter("self.startCurrency.id = ?1 and self.endCurrency.id = ?2 and (self.toDate = null OR  self.toDate >= ?3))",ccl.getStartCurrency().getId(),ccl.getEndCurrency().getId(),ccl.getFromDate()).count() > 0) {
			response.setFlash("ATTENTION : Veuillez clôturer la période actuelle de conversion pour en créer une nouvelle.");
			response.setValue("fromDate", "");
		}
		
		if(ccl.getFromDate() != null && ccl.getToDate() != null && ccl.getFromDate().isAfter(ccl.getToDate())){
			response.setFlash("La date de fin doit impérativement être égale ou supérieur à la date de début.");
			response.setValue("fromDate", "");
		}
	
	}
	
	public void applyConversionRate(ActionRequest request, ActionResponse response) {
		Context context = request.getContext();
		
		LOG.debug("Apply Conversion Rate Context: {}",new Object[]{context});
		
		HashMap currencyFrom = (HashMap)context.get("fromCurrency");
		HashMap currencyTo = (HashMap)context.get("toCurrency");
		
		if(currencyFrom.get("id") != null && currencyTo.get("id") != null){
			Currency fromCurrency = Currency.find(Long.parseLong(currencyFrom.get("id").toString()));
			Currency toCurrency  = Currency.find(Long.parseLong(currencyTo.get("id").toString()));
			CurrencyConversionLine ccl = CurrencyConversionLine.all().filter("startCurrency = ?1 AND endCurrency = ?2",context.get("fromCurrency"),context.get("toCurrency")).order("-fromDate").fetchOne();
			LocalDate today = gs.getTodayDate();
			
			if(ccl != null && ccl.getToDate() == null){
				ccl.setToDate(today.minusDays(1));
				ccs.saveCurrencyConversionLine(ccl);
			}
			BigDecimal rate = new BigDecimal(context.get("conversionRate").toString());
			String variation = null;
		    if(ccl != null)
		    	variation = ccs.getVariations(rate, ccl.getConversionRate());
		    
		    General general = null;
		    if(context.get("general") != null && ((HashMap)context.get("general")).get("id") != null)
		    	general = General.find(Long.parseLong(((HashMap)context.get("general")).get("id").toString()));
			ccs.createCurrencyConversionLine(fromCurrency,toCurrency,today,rate,general,variation);
		}
		else
			response.setFlash("Both currencies must be saved before apply");
		
	}
	
	
}
