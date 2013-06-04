package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.supplychain.service.PurchaseOrderLineService;
import com.axelor.apps.supplychain.db.PurchaseOrder
import com.axelor.apps.supplychain.db.PurchaseOrderLine
import com.axelor.exception.service.TraceBackService

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class PurchaseOrderLineController {
	@Inject
	private PurchaseOrderLineService purchaseOrderLineService
	
	def void compute(ActionRequest request, ActionResponse response){
		
		PurchaseOrderLine purchaseOrderLine = request.context as PurchaseOrderLine
		BigDecimal exTaxTotal = 0.0
		
		try{
		
			if (purchaseOrderLine.price != null && purchaseOrderLine.qty != null){
				
				exTaxTotal = purchaseOrderLineService.computeAmount(purchaseOrderLine.qty, purchaseOrderLine.price)
			}
			
			response.values = ["exTaxTotal": exTaxTotal]
		}
		catch(Exception e)  {
			response.flash = e
		}
	
	}
	
	
	def void getProductInformation(ActionRequest request, ActionResponse response){
		
		PurchaseOrderLine purchaseOrderLine = request.context as PurchaseOrderLine
		
		PurchaseOrder purchaseOrder = purchaseOrderLine.purchaseOrder
		
		if(purchaseOrder == null)  {
			purchaseOrder = request.context.parentContext as PurchaseOrder
		}
			
		if(purchaseOrder != null && purchaseOrderLine.product != null)  {
			
			try  {
				response.values = ["vatLine" : purchaseOrderLineService.getVatLine(purchaseOrder, purchaseOrderLine),
					"price" : purchaseOrderLineService.getUnitPrice(purchaseOrder, purchaseOrderLine),
					"productName" : purchaseOrderLine?.product?.name,
					"unit" : purchaseOrderLine?.product?.unit]
			}
			catch(Exception e)  {
				response.flash = e
			}
		}
	}
}
