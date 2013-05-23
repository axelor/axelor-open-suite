package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.supplychain.db.SalesOrder
import com.axelor.apps.supplychain.service.SalesOrderService
import com.axelor.exception.service.TraceBackService

import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class SalesOrderController {
	
	@Inject
	private SalesOrderService salesOrderService
	
	def void compute(ActionRequest request, ActionResponse response)  {
		
		SalesOrder salesOrder = request.context as SalesOrder

		try {
			
			salesOrderService.computeSalesOrder(salesOrder)
			response.reload = true
			response.flash = "Montant du devis : ${salesOrder.inTaxTotal} TTC"
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
}
