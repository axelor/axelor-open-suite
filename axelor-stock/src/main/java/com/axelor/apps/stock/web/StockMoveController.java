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
package com.axelor.apps.stock.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.report.IReport;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class StockMoveController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private StockMoveService stockMoveService;
	
	@Inject
	private StockMoveRepository stockMoveRepo;

	@Inject
	protected GeneralService generalService;

	public void plan(ActionRequest request, ActionResponse response) {

		StockMove stockMove = request.getContext().asType(StockMove.class);
		try {
			stockMoveService.plan(stockMoveRepo.find(stockMove.getId()));
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}

	public void realize(ActionRequest request, ActionResponse response)  {

		StockMove stockMoveFromRequest = request.getContext().asType(StockMove.class);

		try {
			StockMove stockMove = stockMoveRepo.find(stockMoveFromRequest.getId());
			String newSeq = stockMoveService.realize(stockMove);
			
			response.setReload(true);

			if(newSeq != null)  {
				if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING){
					response.setFlash(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_INCOMING_PARTIAL_GENERATED), newSeq));
				}else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING){
					response.setFlash(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_OUTGOING_PARTIAL_GENERATED), newSeq));
				}else{
					response.setFlash(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_9), newSeq));
				}
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}

	public void cancel(ActionRequest request, ActionResponse response)  {

		StockMove stockMove = request.getContext().asType(StockMove.class);

		try {
			stockMoveService.cancel(stockMoveRepo.find(stockMove.getId()));
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}


	/**
	 * Method to generate stock move as a pdf
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void printStockMove(ActionRequest request, ActionResponse response) throws AxelorException {


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
			stockMove = stockMoveRepo.find(new Long(lstSelectedMove.get(0)));
		}else if(stockMove.getId() != null){
			stockMoveIds = stockMove.getId().toString();
		}

		if(!stockMoveIds.equals("")){

			String language="";
			try{
				language = stockMove.getPartner().getLanguageSelect() != null? stockMove.getPartner().getLanguageSelect() : stockMove.getCompany().getPrintingSettings().getLanguageSelect() != null ? stockMove.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
			}catch (NullPointerException e) {
				language = "en";
			}
			language = language.equals("")? "en": language;

			String title = I18n.get("Stock move");
			if(stockMove.getStockMoveSeq() != null)  {
				title = lstSelectedMove == null ? I18n.get("StockMove") + " " + stockMove.getStockMoveSeq() : I18n.get("StockMove(s)");
			}

			String fileLink = ReportFactory.createReport(IReport.STOCK_MOVE, title+"-${date}")
					.addParam("StockMoveId", stockMoveIds)
					.addParam("Locale", language)
					.generate()
					.getFileLink();

			logger.debug("Printing "+title);
		
			response.setView(ActionView
					.define(title)
					.add("html", fileLink).map());
				
		}else{
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_10));
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
			msg = I18n.get(IExceptionMessage.STOCK_MOVE_11);
		if (generalService.getGeneral().getMapApiSelect() == IAdministration.MAP_API_OSM)
			msg = I18n.get(IExceptionMessage.STOCK_MOVE_12);
		if(msg.isEmpty()){
			String dString = fromAddress.getAddressL4()+" ,"+fromAddress.getAddressL6();
			String aString = toAddress.getAddressL4()+" ,"+toAddress.getAddressL6();
			BigDecimal dLat = fromAddress.getLatit();
			BigDecimal dLon = fromAddress.getLongit();
			BigDecimal aLat = toAddress.getLatit();
			BigDecimal aLon =  toAddress.getLongit();
			Map<String, Object> result = Beans.get(MapService.class).getDirectionMapGoogle(dString, dLat, dLon, aString, aLat, aLon);
			if(result != null){
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", I18n.get("Map"));
				mapView.put("resource", result.get("url"));
				mapView.put("viewType", "html");
			    response.setView(mapView);
			}
			else response.setFlash(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_13),dString,aString));
		}else response.setFlash(msg);

	}

	public void  splitStockMoveLinesUnit(ActionRequest request, ActionResponse response) {
		List<StockMoveLine> stockMoveLines = (List<StockMoveLine>) request.getContext().get("stockMoveLineList");
		if(stockMoveLines == null){
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_14));
			return;
		}
		Boolean selected = stockMoveService.splitStockMoveLinesUnit(stockMoveLines, new BigDecimal(1));

		if(!selected)
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_15));
		response.setReload(true);
		response.setCanClose(true);
	}

	public void  splitStockMoveLinesSpecial(ActionRequest request, ActionResponse response) {
		List<HashMap> stockMoveLines = (List<HashMap>) request.getContext().get("stockMoveLineList");
		if(stockMoveLines == null){
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_14));
			return;
		}
		Integer splitQty = (Integer)request.getContext().get("splitQty");
		if(splitQty < 1){
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_16));
			return ;
		}
		Boolean selected = stockMoveService.splitStockMoveLinesSpecial(stockMoveLines, new BigDecimal(splitQty));
		if(!selected)
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_15));
		response.setReload(true);
		response.setCanClose(true);
	}

	public void shipReciveAllProducts(ActionRequest request, ActionResponse response) {
		StockMove stockMove = request.getContext().asType(StockMove.class);
		stockMoveService.copyQtyToRealQty(stockMoveRepo.find(stockMove.getId()));
		response.setReload(true);
	}

	public void generateReversion(ActionRequest request, ActionResponse response)  {

		StockMove stockMove = request.getContext().asType(StockMove.class);

		try {
			stockMoveService.generateReversion(stockMoveRepo.find(stockMove.getId()));
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}

	public void  splitInto2(ActionRequest request, ActionResponse response) {
		StockMove stockMove = request.getContext().asType(StockMove.class);
		Long newStockMoveId = stockMoveService.splitInto2(stockMove.getId(), stockMove.getStockMoveLineList());

		if (newStockMoveId == null){
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_SPLIT_NOT_GENERATED));
		}else{
			response.setCanClose(true);

			response.setView(ActionView
					.define("Stock move")
					.model(StockMove.class.getName())
					.add("grid", "stock-move-grid")
					.add("form", "stock-move-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(newStockMoveId)).map());

		}

	}
	
	public void  compute(ActionRequest request, ActionResponse response) {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);
		response.setValue("exTaxTotal", stockMoveService.compute(stockMove));
		
	}

}
