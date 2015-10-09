package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class StockMoveLineController {
	
	@Inject
	protected StockMoveLineService stockMoveLineService;
	
	public void compute(ActionRequest request, ActionResponse response) throws AxelorException {
		StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
		StockMove stockMove = stockMoveLine.getStockMove();
		if(stockMove == null){
			stockMove = request.getContext().getParentContext().asType(StockMove.class);
		}
		stockMoveLine.getStockMove();
		stockMoveLine = stockMoveLineService.compute(stockMoveLine, stockMove);
		response.setValue("unitPriceUntaxed", stockMoveLine.getUnitPriceUntaxed());
		response.setValue("unitPriceTaxed", stockMoveLine.getUnitPriceTaxed());
	}
}
