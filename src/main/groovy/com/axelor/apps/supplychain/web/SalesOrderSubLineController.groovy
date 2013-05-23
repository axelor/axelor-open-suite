package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.supplychain.db.SalesOrder
import com.axelor.apps.supplychain.db.SalesOrderSubLine
import com.axelor.apps.supplychain.service.SalesOrderLineService;
import com.axelor.apps.supplychain.service.SalesOrderService
import com.axelor.apps.supplychain.service.SalesOrderSubLineService
import com.axelor.exception.service.TraceBackService

import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class SalesOrderSubLineController {
	
	@Inject
	private SalesOrderSubLineService salesOrderSubLineService
	
	def void compute(ActionRequest request, ActionResponse response){
		
		SalesOrderSubLine salesOrderSubLine = request.context as SalesOrderSubLine
		BigDecimal exTaxTotal = 0.0
		
		try{
		
			if (salesOrderSubLine.price != null && salesOrderSubLine.qty != null){
				
				exTaxTotal = salesOrderSubLineService.computeAmount(salesOrderSubLine.qty, salesOrderSubLine.price)
			}
			
			response.values = ["exTaxTotal": exTaxTotal]
		}
		catch(Exception e)  {
			response.flash = e
		}
	
	}
	
	
	def void getProductInformation(ActionRequest request, ActionResponse response){
		
		SalesOrderSubLine salesOrderSubLine = request.context as SalesOrderSubLine
		
		SalesOrder salesOrder = salesOrderSubLine?.salesOrderLine?.salesOrder
		
		if(salesOrder == null)  {
			salesOrder = request.context.parentContext.parentContext as SalesOrder
		}
			
		if(salesOrder != null && salesOrderSubLine.product != null)  {
			
			try  {
				response.values = ["vatLine" : salesOrderSubLineService.getVatLine(salesOrder, salesOrderSubLine),
					"price" : salesOrderSubLineService.getUnitPrice(salesOrder, salesOrderSubLine),
					"productName" : salesOrderSubLine?.product?.name]
			}
			catch(Exception e)  {
				response.flash = e
			}
		}
	}
	
}
