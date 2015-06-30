package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceController {

	@Inject
	protected SaleOrderInvoiceService saleOrderInvoiceService;

	public void fillInLines(ActionRequest request, ActionResponse response) {
		Invoice invoice = request.getContext().asType(Invoice.class);
		saleOrderInvoiceService.fillInLines(invoice);
		response.setValues(invoice);
	}
}
