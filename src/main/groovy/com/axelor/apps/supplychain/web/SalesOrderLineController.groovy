package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.supplychain.db.SalesOrder
import com.axelor.apps.supplychain.db.SalesOrderLine
import com.axelor.apps.supplychain.service.SalesOrderLineService
import com.axelor.apps.supplychain.service.SalesOrderService
import com.axelor.exception.service.TraceBackService

import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class SalesOrderLineController {
	
	@Inject
	private SalesOrderLineService salesOrderLineService
	
	def void compute(ActionRequest request, ActionResponse response){
		
		SalesOrderLine salesOrderLine = request.context as SalesOrderLine
		BigDecimal exTaxTotal = 0.0
		
		try{
		
			if (salesOrderLine.price != null && salesOrderLine.qty != null){
				
				if(salesOrderLine.salesOrderSubLineList == null || salesOrderLine.salesOrderSubLineList.isEmpty())  {
					exTaxTotal = salesOrderLineService.computeAmount(salesOrderLine.qty, salesOrderLine.price)
				}
			}
			
			response.values = ["exTaxTotal": exTaxTotal]
		}
		catch(Exception e)  {
			response.flash = e
		}
	
	}
	
	
	def void getProductInformation(ActionRequest request, ActionResponse response){
		
		SalesOrderLine salesOrderLine = request.context as SalesOrderLine
		
		SalesOrder salesOrder = salesOrderLine.salesOrder
		
		if(salesOrder == null)  {
			salesOrder = request.context.parentContext as SalesOrder
		}
			
		if(salesOrder != null && salesOrderLine.product != null)  {
			
			try  {
				response.values = ["vatLine" : salesOrderLineService.getVatLine(salesOrder, salesOrderLine),
					"price" : salesOrderLineService.getUnitPrice(salesOrder, salesOrderLine),
					"productName" : salesOrderLine?.product?.name,
					"unit" : salesOrderLine?.product?.unit]
			}
			catch(Exception e)  {
				response.flash = e
			}
		}
	}
	
	def void setBooleans(ActionRequest request, ActionResponse response){
		
		SalesOrderLine salesOrderLine = request.context as SalesOrderLine
		
		SalesOrder salesOrder = salesOrderLine.salesOrder
		
		if(salesOrder == null)  {
			salesOrder = request.context.parentContext as SalesOrder
		}
			
		if(salesOrder != null)  {
			
			def values = [ : ]
			
			try  {
				if(salesOrder.getHasToCreateTaskByLine()) {
					values.put("hasToCreateTask", true)
				}
				else {
					values.put("hasToCreateTask", false)
				}
				
				if(salesOrder.getHasToCreateProposal()) {
					values.put("hasToBuy", true)
				}
				else {
					values.put("hasToBuy", false)
				}
				
				if(salesOrder.getIsToPrintLineSubTotal()){
					values.put("isToPrintLineSubTotal", true)
				}
				else {
					values.put("isToPrintLineSubTotal", false)
				}
				response.setValues(values)
			}
			catch(Exception e)  {
				response.flash = e
			}
		}
	}
}
