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
package com.axelor.apps.account.web;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.service.debtrecovery.PayerQualityService;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class GeneralController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);

	@Inject
	private Injector injector;
	
	@Inject
	private GeneralService gs;
	
	@Inject 
	private CurrencyConversionService ccs;
	
	@Inject
	private CurrencyConversionLineRepository cclRepo;
	
	
	
	public void payerQualityProcess(ActionRequest request, ActionResponse response)  {
		
		try  {
			PayerQualityService pqs = injector.getInstance(PayerQualityService.class);
			pqs.payerQualityProcess();
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}
	
	public void updateCurrencyConversion(ActionRequest request, ActionResponse response){
		 General general = request.getContext().asType(General.class);
		 LocalDate today = GeneralService.getTodayDate();
		 
		 for(CurrencyConversionLine ccl : general.getCurrencyConversionLineList()){
			CurrencyConversionLine cclCoverd = cclRepo.all().filter("startCurrency = ?1 AND endCurrency = ?2 AND fromDate >= ?3 AND (toDate <= ?3 OR toDate = null)",ccl.getStartCurrency(),ccl.getEndCurrency(),today).fetchOne();
			LOG.info("Currency Conversion Line for {} already covered : {}",today,ccl);
			if(ccl.isSelected() && ccl.getToDate() == null & cclCoverd == null){
				BigDecimal currentRate = ccs.convert(ccl.getStartCurrency(), ccl.getEndCurrency());
				if(currentRate.compareTo(new BigDecimal(-1)) == 0){
					response.setFlash("Currency conversion webservice not working");
					break;
				}
				ccl = cclRepo.find(ccl.getId());
				ccl.setToDate(today.minusDays(1));
				ccs.saveCurrencyConversionLine(ccl);
				BigDecimal previousRate = ccl.getExchangeRate();
				String variations = ccs.getVariations(currentRate, previousRate);
				ccs.createCurrencyConversionLine(ccl.getStartCurrency(), ccl.getEndCurrency(), today, currentRate, gs.find(general.getId()), variations);
			}
		 }
		 response.setReload(true);
	}
}
