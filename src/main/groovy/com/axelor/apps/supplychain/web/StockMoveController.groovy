package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.supplychain.db.StockMove
import com.axelor.apps.supplychain.service.StockMoveService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class StockMoveController {
	
	@Inject
	private StockMoveService stockMoveService
	
	
	def void plan(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.context as StockMove

		try {
			
			stockMoveService.plan(StockMove.find(stockMove.id))
			
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	
	def void realize(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.context as StockMove

		try {
			
			stockMoveService.realize(StockMove.find(stockMove.id))
			
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	def void cancel(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.context as StockMove

		try {
			
			stockMoveService.cancel(StockMove.find(stockMove.id))
			
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
}
