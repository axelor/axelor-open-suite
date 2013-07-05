package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.base.db.Product
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
					"saleSupplySelect" : salesOrderLine?.product?.saleSupplySelect,
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
			Product product = salesOrderLine.getProduct()
			
			try  {				
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
	
	def void setSequence(ActionRequest request, ActionResponse response){
		
		SalesOrderLine salesOrderLine = request.context as SalesOrderLine
		
		SalesOrder salesOrder = salesOrderLine.salesOrder
		
		if(salesOrder == null)  {
			salesOrder = request.context.parentContext as SalesOrder
		}
			
		if(salesOrder != null)  {
			
			try  {
				int sizeList = 0
				
				if(salesOrder.getSalesOrderLineList() != null) {
					sizeList = salesOrder.getSalesOrderLineList().size()
				}
				if(sizeList == 0) {
					
					response.values = ["sequence" : 1]
				}
				else if(sizeList > 0) {
					
					response.values = ["sequence" : sizeList+1]
				}
			}
			catch(Exception e)  {
				response.flash = e
			}
		}
	}
}
