package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class StockMoveInvoiceController {

	@Inject
	private StockMoveInvoiceService stockMoveInvoiceService;
	
	public void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);
		Invoice invoice = null;
		int numInvoice = 0;
		try {
			stockMove = StockMove.find(stockMove.getId());
			
			if(stockMove.getSalesOrder() != null) {
				invoice = stockMoveInvoiceService.createInvoiceFromSalesOrder(stockMove, stockMove.getSalesOrder());
				if(invoice != null)
					numInvoice++;
			}
			
			if(stockMove.getPurchaseOrder() != null) {
				invoice = stockMoveInvoiceService.createInvoiceFromPurchaseOrder(stockMove, stockMove.getPurchaseOrder());
				if(invoice != null)
					numInvoice++;
			}
			
			if(numInvoice > 0)  {
				response.setReload(true);
				if(numInvoice == 1)
					response.setFlash("1 Facture créée");
				else
					response.setFlash(numInvoice+" Factures créées");
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
