/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.apps.stock.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class StockMoveController {
	
	private static final Logger LOG = LoggerFactory.getLogger(StockMoveController.class);

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
			String newSeq = stockMoveService.realize(StockMove.find(stockMove.getId()));
			
			response.setReload(true);
			
			if(newSeq != null)  {
				
				response.setFlash(String.format("A partial stock move has been generated (%s)", newSeq));
				
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
			stockMoveIds = stockMoveIds.substring(0, stockMoveIds.length()-1);	
			stockMove = StockMove.find(new Long(lstSelectedMove.get(0)));
		}else if(stockMove.getId() != null){
			stockMoveIds = stockMove.getId().toString();			
		}
		
		if(!stockMoveIds.equals("")){
			StringBuilder url = new StringBuilder();			
			
			String language="";
			try{
				language = stockMove.getPartner().getLanguageSelect() != null? stockMove.getPartner().getLanguageSelect() : stockMove.getCompany().getPrintingSettings().getLanguageSelect() != null ? stockMove.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
			}catch (NullPointerException e) {
				language = "en";
			}
			language = language.equals("")? "en": language;
			
			url.append(
					new ReportSettings(IReport.STOCK_MOVE)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("StockMoveId", stockMoveIds)
					.getUrl());
			
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
	
	public void generateReversion(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);

		try {
			stockMoveService.generateReversion(StockMove.find(stockMove.getId()));		
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
}
