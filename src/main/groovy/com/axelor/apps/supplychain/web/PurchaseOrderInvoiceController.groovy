package com.axelor.apps.supplychain.web

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.supplychain.db.PurchaseOrder
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.exception.service.TraceBackService
import com.google.inject.Inject;

import groovy.util.logging.Slf4j;

@Slf4j
class PurchaseOrderInvoiceController {

	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
	
	def void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		PurchaseOrder purchaseOrder = request.context as PurchaseOrder
		
		try {
			Invoice invoice = purchaseOrderInvoiceService.generateInvoice(purchaseOrder)
			
			if(invoice != null)  {
				response.reload = true
				response.flash = "Facture créée"
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
}
