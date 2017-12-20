/**
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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockMoveController {

	@Inject
	private StockMoveService stockMoveService;
	
	@Inject
	private StockMoveRepository stockMoveRepo;

	@Inject
	protected AppBaseService appBaseService;
	

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
			stockMoveService.cancel(stockMoveRepo.find(stockMove.getId()), stockMove.getCancelReason());
			response.setCanClose(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}


	/**
	 * Method called from stock move form and grid view.
	 * Print one or more stock move as PDF
	 * @param request
	 * @param response
	 */
	public void printStockMove(ActionRequest request, ActionResponse response) {
		StockMove stockMove = request.getContext().asType(StockMove.class);
		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedMove = (List<Integer>) request.getContext().get("_ids");

		try {
			String fileLink = stockMoveService.printStockMove(stockMove, lstSelectedMove, false);
			response.setView(ActionView
					.define(I18n.get("Stock move"))
					.add("html", fileLink).map());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	/**
	 * Method called from stock move form and grid view.
	 * Print one or more stock move as PDF
	 * @param request
	 * @param response
	 */
	public void printPickingStockMove(ActionRequest request, ActionResponse response) {
		StockMove stockMove = request.getContext().asType(StockMove.class);
		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedMove = (List<Integer>) request.getContext().get("_ids");

		try {
			String fileLink = stockMoveService.printStockMove(stockMove, lstSelectedMove, true);
			response.setView(ActionView
					.define(I18n.get("Stock move"))
					.add("html", fileLink).map());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}



	public void  viewDirection(ActionRequest request, ActionResponse response) {

		StockMove stockMove = request.getContext().asType(StockMove.class);
		try {
			Map<String, Object> result = Beans.get(StockMoveService.class)
					.viewDirection(stockMove);
			Map<String,Object> mapView = new HashMap<>();
			mapView.put("title", I18n.get("Map"));
			mapView.put("resource", result.get("url"));
			mapView.put("viewType", "html");
			response.setView(mapView);
		} catch (Exception e) {
		    response.setFlash(e.getLocalizedMessage());
		}
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void  splitStockMoveLinesSpecial(ActionRequest request, ActionResponse response) {
		List<HashMap> stockMoveLines = (List<HashMap>) request.getContext().get("stockMoveLineList");
		if(stockMoveLines == null){
			response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_14));
			return;
		}
		Integer splitQty = (Integer)request.getContext().get("splitQty");
		if(splitQty != null && splitQty < 1){
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
			StockMove reversion = stockMoveService.generateReversion(stockMoveRepo.find(stockMove.getId()));
			response.setView(ActionView
					.define(I18n.get("Stock move"))
					.model(StockMove.class.getName())
					.add("grid", "stock-move-grid")
					.add("form", "stock-move-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(reversion.getId())).map());
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

	public void changeConformityStockMove(ActionRequest request, ActionResponse response) {
		StockMove stockMove = request.getContext().asType(StockMove.class);
		response.setValue("stockMoveLineList", stockMoveService.changeConformityStockMove(stockMove));
	}

	public void changeConformityStockMoveLine(ActionRequest request, ActionResponse response) {
		StockMove stockMove = request.getContext().asType(StockMove.class);
		response.setValue("conformitySelect", stockMoveService.changeConformityStockMoveLine(stockMove));
	}

	public void  compute(ActionRequest request, ActionResponse response) {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);
		response.setValue("exTaxTotal", stockMoveService.compute(stockMove));
		
	}
	
	public void openStockPerDay(ActionRequest request, ActionResponse response) {
		
		Context context = request.getContext();
		
		Long locationId = Long.parseLong(((Map<String,Object>)context.get("stockLocation")).get("id").toString());
		LocalDate fromDate = LocalDate.parse(context.get("stockFromDate").toString());
		LocalDate toDate = LocalDate.parse(context.get("stockToDate").toString());
		
		Collection<Map<String,Object>> products = (Collection<Map<String,Object>>)context.get("productSet");
		
		String domain = null;
		List<Object> productIds = null;
		if (products != null && !products.isEmpty()) {
			productIds = Arrays.asList(products.stream().map(p->p.get("id")).toArray());
			domain = "self.id in (:productIds)";
		}
		
		response.setView(ActionView.define(I18n.get("Stocks"))
			.model(Product.class.getName())
			.add("cards", "stock-product-cards")
			.add("grid", "stock-product-grid")
			.add("form", "stock-product-form")
			.domain(domain)
			.context("fromStockWizard", true)
			.context("productIds", productIds)
			.context("stockFromDate", fromDate)
			.context("stockToDate", toDate)
			.context("locationId", locationId)
			.map());
		
	}

	public void fillAddressesStr(ActionRequest request, ActionResponse response) {
	    StockMove stockMove = request.getContext().asType(StockMove.class);
	    stockMoveService.computeAddressStr(stockMove);

	    response.setValues(stockMove);
	}

}
