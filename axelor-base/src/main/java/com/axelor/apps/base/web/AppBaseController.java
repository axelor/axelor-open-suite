/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.app.AppSettings;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.ExportDbObjectService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AppBaseController {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private ExportDbObjectService eos;
	
	@Inject
	private MapService mapService;
	
	@Inject
	private CurrencyConversionService ccs;

	@Inject
	private CurrencyConversionLineRepository cclRepo;
	
	@Inject
	private AppBaseService appBaseService;

	public void exportObjects(ActionRequest request, ActionResponse response){
		MetaFile metaFile = eos.exportObject();
		if(metaFile == null){
			response.setFlash(I18n.get(IExceptionMessage.GENERAL_4));
		}
		else {
			response.setView(ActionView
					  .define(I18n.get(IExceptionMessage.GENERAL_5))
					  .model("com.axelor.meta.db.MetaFile")
					  .add("form", "meta-files-form")
					  .add("grid", "meta-files-grid")
					  .param("forceEdit", "true")
					  .context("_showRecord", metaFile.getId().toString())
					  .map());
		}
	}
	
	public void checkMapApi(ActionRequest request, ActionResponse response)  {
		
		AppBase appBase = request.getContext().asType(AppBase.class);;
		
		boolean connected = false;
		
		Integer apiType = appBase.getMapApiSelect();
		
		if(apiType == 1){
			connected = mapService.testGMapService();
		}
		
		if(connected){
			response.setFlash(IExceptionMessage.GENERAL_6);
		}
		else{
			response.setFlash(IExceptionMessage.GENERAL_7);
		}
	}
	
	public void updateCurrencyConversion(ActionRequest request, ActionResponse response){
		 AppBase appBase = request.getContext().asType(AppBase.class);
		 LocalDate today = appBaseService.getTodayDate();
		 
		 Map<Long, Long> currencyMap = new HashMap<Long, Long>();
		 
		 for(CurrencyConversionLine ccl : appBase.getCurrencyConversionLineList()){
			 currencyMap.put(ccl.getEndCurrency().getId(), ccl.getStartCurrency().getId());
		 }
		 
		 for(Long key  : currencyMap.keySet()){
			
			CurrencyConversionLine ccl = cclRepo.all().filter("startCurrency.id = ?1 AND endCurrency.id = ?2 AND fromDate <= ?3 AND toDate is null", currencyMap.get(key), key, today).fetchOne();
			
			LOG.info("Currency Conversion Line without toDate : {}", ccl);

			if(ccl == null){
				ccl = cclRepo.all().filter("startCurrency.id = ?1 AND endCurrency.id = ?2 AND fromDate <= ?3 AND toDate > ?3", currencyMap.get(key), key, today).fetchOne();
				if(ccl != null){
					LOG.info("Already convered Currency Conversion Line  found : {}", ccl);
					continue;
				}
				ccl = cclRepo.all().filter("startCurrency.id = ?1 AND endCurrency.id = ?2 AND fromDate <= ?3 AND (toDate not null AND toDate <= ?3)", currencyMap.get(key), key, today).order("-toDate").fetchOne();
				LOG.info("Currency Conversion Line found with toDate : {}", ccl);
			}
			
			
			if(ccl != null){
				BigDecimal currentRate = ccs.convert(ccl.getStartCurrency(), ccl.getEndCurrency());
				if(currentRate.compareTo(new BigDecimal(-1)) == 0){
					response.setFlash(I18n.get(IExceptionMessage.CURRENCY_6));
					break;
				}
				ccl = cclRepo.find(ccl.getId());
				ccl.setToDate(today.minusDays(1));
				ccs.saveCurrencyConversionLine(ccl);
				BigDecimal previousRate = ccl.getExchangeRate();
				String variations = ccs.getVariations(currentRate, previousRate);
				ccs.createCurrencyConversionLine(ccl.getStartCurrency(), ccl.getEndCurrency(), today, currentRate, appBaseService.getAppBase(), variations);
			}
			
		 }
		 
		 response.setReload(true);
	}
	
	public void applyApplicationMode(ActionRequest request, ActionResponse response)  {
		 String applicationMode = AppSettings.get().get("application.mode", "prod");
		 if ("dev".equals(applicationMode)) {
			 response.setAttr("main", "hidden", false);
		 }
	}

	
	public void showCustomersOnMap(ActionRequest request, ActionResponse response) throws AxelorException {

		mapService.showMap("customer", I18n.get("Customers"), response);
	
	}
		 	
	public void showProspectsOnMap(ActionRequest request, ActionResponse response) throws AxelorException {
		
		mapService.showMap("prospect", I18n.get("Prospect"), response);
 	
	}
		 	
 	public void showSuppliersOnMap(ActionRequest request, ActionResponse response) throws AxelorException {
 
 		mapService.showMap("supplier", I18n.get("Supplier"), response);
 	
 	}
 	
		 	
}
