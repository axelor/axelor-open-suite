package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderInvoiceController {

	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
	
	public void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		try {
			Invoice invoice = purchaseOrderInvoiceService.generateInvoice(purchaseOrder);
			
			if(invoice != null)  {
				response.setReload(true);
				response.setFlash("Facture créée");
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
