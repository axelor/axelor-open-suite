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
		Invoice invoice = null
		int numInvoice = 0
		try {
			
			stockMove = StockMove.find(stockMove.getId())
			
			if(stockMove.salesOrder) {
				invoice = stockMoveInvoiceService.createInvoiceFromSalesOrder(stockMove, stockMove.salesOrder)
				if(invoice)
					numInvoice++
			}
			
			if(stockMove.purchaseOrder) {
				invoice = stockMoveInvoiceService.createInvoiceFromPurchaseOrder(stockMove, stockMove.purchaseOrder)
				if(invoice)
					numInvoice++
			}
			
			if(numInvoice > 0)  {
				response.reload = true
				if(numInvoice == 1)
					response.flash = "$numInvoice Facture créée"
				else
					response.flash = "$numInvoice Factures créées"
			}
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
}
