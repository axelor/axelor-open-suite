/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
package com.axelor.apps.supplychain.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
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
	
	@Inject
	private AddressService ads;
	
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
			StockMove newStockMove = stockMoveService.realize(StockMove.find(stockMove.getId()));
			
			response.setReload(true);
			
			if(newStockMove != null)  {
				
				response.setFlash(String.format("A partial stock move has been generated (%s)", newStockMove.getStockMoveSeq()));
				
			}
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
		String stockMoveIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedMove = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedMove != null){
			for(Integer it : lstSelectedMove) {
				stockMoveIds+= it.toString()+",";
			}
		}	
			
		if(!stockMoveIds.equals("")){
			stockMoveIds = "&StockMoveId="+stockMoveIds.substring(0, stockMoveIds.length()-1);	
			stockMove = StockMove.find(new Long(lstSelectedMove.get(0)));
		}else if(stockMove.getId() != null){
			stockMoveIds = "&StockMoveId="+stockMove.getId();			
		}
		
		if(!stockMoveIds.equals("")){
			StringBuilder url = new StringBuilder();			
			AxelorSettings axelorSettings = AxelorSettings.get();
			
			String language="";
			try{
				language = stockMove.getPartner().getLanguageSelect() != null? stockMove.getPartner().getLanguageSelect() : stockMove.getCompany().getPrintingSettings().getLanguageSelect() != null ? stockMove.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
			}catch (NullPointerException e) {
				language = "en";
			}
			language = language.equals("")? "en": language;

			url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/StockMove.rptdesign&__format=pdf&Locale="+language+stockMoveIds+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression du stock move "+stockMove.getStockMoveSeq()+" : "+url.toString());
				
				String title = " ";
				if(stockMove.getStockMoveSeq() != null)  {
					title += lstSelectedMove == null ? "StockMove "+stockMove.getStockMoveSeq():"StockMove(s)";
				}
				
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", title);
				mapView.put("resource", url);
				mapView.put("viewType", "html");
				response.setView(mapView);	
					
			}
			else {
				response.setFlash(urlNotExist);
			}
		}else{
			response.setFlash("Please select the StockMove(s) to print.");
		}	
	}
	
	
	
	
	public void  viewDirection(ActionRequest request, ActionResponse response) {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);
		
		Address fromAddress = stockMove.getFromAddress();
		Address toAddress = stockMove.getToAddress();
		String msg = "";
		if(fromAddress == null)
			fromAddress =  stockMove.getCompany().getAddress();
		if(toAddress == null)
			toAddress =  stockMove.getCompany().getAddress();
		if(fromAddress == null || toAddress == null)
			msg = "Company address is empty.";
		if (GeneralService.getGeneral().getMapApiSelect() == IAdministration.MAP_API_OSM)
			msg = "Feature currently not available with Open Street Maps.";
		if(msg.isEmpty()){
			String dString = fromAddress.getAddressL4()+" ,"+fromAddress.getAddressL6();
			String aString = toAddress.getAddressL4()+" ,"+toAddress.getAddressL6();
			BigDecimal dLat = fromAddress.getLatit();
			BigDecimal dLon = fromAddress.getLongit();
			BigDecimal aLat = toAddress.getLatit();
			BigDecimal aLon =  toAddress.getLongit();
			Map<String, Object> result = ads.getDirectionMapGoogle(dString, dLat, dLon, aString, aLat, aLon);
			if(result != null){
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", "Map");
				mapView.put("resource", result.get("url"));
				mapView.put("viewType", "html");
			    response.setView(mapView);
			}
			else response.setFlash(String.format("<B>%s or %s</B> not found",dString,aString));
		}else response.setFlash(msg);
		
	}
	
	public void  splitStockMoveLinesUnit(ActionRequest request, ActionResponse response) {
		List<StockMoveLine> stockMoveLines = (List<StockMoveLine>) request.getContext().get("stockMoveLineList");
		if(stockMoveLines == null){
			response.setFlash("No move lines to split");
			return;
		}
		Boolean selected = stockMoveService.splitStockMoveLinesUnit(stockMoveLines, new BigDecimal(1));
		
		if(!selected)
			response.setFlash("Please select lines to split");
		response.setReload(true);
		response.setCanClose(true);
	}
	
	public void  splitStockMoveLinesSpecial(ActionRequest request, ActionResponse response) {
		List<HashMap> stockMoveLines = (List<HashMap>) request.getContext().get("stockMoveLineList");
		if(stockMoveLines == null){
			response.setFlash("No move lines to split");
			return;
		}
		Integer splitQty = (Integer)request.getContext().get("splitQty");
		if(splitQty < 1){
			response.setFlash("Please entry proper split qty");
			return ;
		}
		Boolean selected = stockMoveService.splitStockMoveLinesSpecial(stockMoveLines, new BigDecimal(splitQty));
		if(!selected)
			response.setFlash("Please select lines to split");
		response.setReload(true);
		response.setCanClose(true);
	}
	
	public void shipReciveAllProducts(ActionRequest request, ActionResponse response) {
		StockMove stockMove = request.getContext().asType(StockMove.class);
		stockMoveService.copyQtyToRealQty(StockMove.find(stockMove.getId()));
		response.setReload(true);
	}
	
}
