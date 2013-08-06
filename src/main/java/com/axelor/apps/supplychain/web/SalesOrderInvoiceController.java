package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.service.SalesOrderInvoiceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SalesOrderInvoiceController {

	@Inject
	private SalesOrderInvoiceService salesOrdeInvoicerService;
	
	public void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);
		
		try {
			salesOrder = SalesOrder.find(salesOrder.getId());
			Invoice invoice = salesOrdeInvoicerService.generateInvoice(salesOrder);
			
			if(invoice != null)  {
				response.setReload(true);
				response.setFlash("Facture créée");
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
