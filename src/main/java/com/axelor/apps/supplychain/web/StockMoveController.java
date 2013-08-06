package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.service.StockMoveService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class StockMoveController {

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
}
