package com.axelor.apps.account.web;

import java.math.BigDecimal;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceLineController {

	@Inject
	private InvoiceLineService invoiceLineService;

	public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal accountingExTaxTotal = BigDecimal.ZERO;

		if(invoiceLine.getPrice() != null && invoiceLine.getQty() != null) {

			exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), invoiceLine.getPrice());
		}

		if(exTaxTotal != null) {

			Invoice invoice = invoiceLine.getInvoice();

			if(invoice == null) {
				invoice = request.getContext().getParentContext().asType(Invoice.class);
			}

			if(invoice != null) {
				accountingExTaxTotal = invoiceLineService.getAccountingExTaxTotal(exTaxTotal, invoice);
			}
		}
		response.setValue("exTaxTotal", exTaxTotal);
		response.setValue("accountingExTaxTotal", accountingExTaxTotal);

	}

	public void getProductInformation(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

		Invoice invoice = invoiceLine.getInvoice();

		if(invoice == null)  {
			invoice = request.getContext().getParentContext().asType(Invoice.class);
		}

		if(invoice != null && invoiceLine.getProduct() != null)  {

			boolean isPurchase = invoiceLineService.isPurchase(invoice);
			response.setValue("vatLine", invoiceLineService.getVatLine(invoice, invoiceLine, isPurchase));
			response.setValue("price", invoiceLineService.getUnitPrice(invoice, invoiceLine, isPurchase));
			response.setValue("productName", invoiceLine.getProduct().getName());
		}
	}
}
