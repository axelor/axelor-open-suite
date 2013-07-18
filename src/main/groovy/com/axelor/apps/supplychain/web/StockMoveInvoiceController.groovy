package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.supplychain.db.StockMove
import com.axelor.apps.supplychain.service.StockMoveInvoiceService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class StockMoveInvoiceController {
	
	@Inject
	private StockMoveInvoiceService stockMoveInvoiceService
	
	def void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.context as StockMove
		
		try {
			
			stockMove = StockMove.find(stockMove.getId())
			
			Invoice invoice = stockMoveInvoiceService.createInvoice(stockMove, stockMove.salesOrder)
			
			if(invoice != null)  {
				response.reload = true
				response.flash = "Facture créée"
			}
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
}
