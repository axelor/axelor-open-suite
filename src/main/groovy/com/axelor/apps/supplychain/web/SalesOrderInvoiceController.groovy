package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.supplychain.db.SalesOrder
import com.axelor.apps.supplychain.service.SalesOrderInvoiceService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class SalesOrderInvoiceController {
	
	@Inject
	private SalesOrderInvoiceService salesOrdeInvoicerService
	
	def void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		SalesOrder salesOrder = request.context as SalesOrder
		
		try {
			
			salesOrder = SalesOrder.find(salesOrder.getId())
			
			Invoice invoice = salesOrdeInvoicerService.generateInvoice(salesOrder)
			
			if(invoice != null)  {
				response.reload = true
				response.flash = "Facture créée"
			}
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
}
