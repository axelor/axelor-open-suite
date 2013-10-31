/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.service.StockMoveService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class StockMoveController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderController.class);

	@Inject
	private StockMoveService stockMoveService;
	
	public void plan(ActionRequest request, ActionResponse response) {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);

		try {
			stockMoveService.plan(StockMove.find(stockMove.getId()));
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void realize(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);

		try {
			stockMoveService.realize(StockMove.find(stockMove.getId()));
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void cancel(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);

		try {
			stockMoveService.cancel(StockMove.find(stockMove.getId()));		
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printStockMove(ActionRequest request, ActionResponse response) {

		StockMove stockMove = request.getContext().asType(StockMove.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();
		String language = stockMove.getPartner().getLanguageSelect() != null? stockMove.getPartner().getLanguageSelect() : stockMove.getCompany().getPrintingSettings().getLanguageSelect() != null ? stockMove.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		language = language == "" ? "en": language;
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/StockMove.rptdesign&__format=pdf&Locale="+language+"&StockMoveId="+stockMove.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du stock move "+stockMove.getStockMoveSeq()+" : "+url.toString());
			
			String title = "StockMove ";
			if(stockMove.getStockMoveSeq() != null)  {
				title += stockMove.getStockMoveSeq();
			}
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "StockMove "+title);
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
