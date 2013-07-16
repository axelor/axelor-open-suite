package com.axelor.apps.account.web

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.account.db.InvoiceLine
import com.axelor.apps.account.db.VatLine
import com.axelor.apps.account.service.AccountManagementService
import com.axelor.apps.account.service.invoice.InvoiceLineService
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector

class InvoiceLineController {
		
	@Inject
	private InvoiceLineService invoiceLineService
	
	def void compute(ActionRequest request, ActionResponse response){

		InvoiceLine invoiceLine = request.context as InvoiceLine
		BigDecimal exTaxTotal = 0.0
		BigDecimal accountingExTaxTotal = 0.0
		
//		try{
		
			if (invoiceLine.price != null && invoiceLine.qty != null){
				
				exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.qty, invoiceLine.price)
			}
			
			if(exTaxTotal != null)  {
				
				Invoice invoice = invoiceLine.invoice
				
				if(invoice == null)  {
					invoice = request.context.parentContext as Invoice
				}
				
				if(invoice != null)  {
					accountingExTaxTotal = invoiceLineService.getAccountingExTaxTotal(exTaxTotal, invoice)
				}
			}
		
			response.values = ["exTaxTotal": exTaxTotal,
								"accountingExTaxTotal" : accountingExTaxTotal]
//		}
//		catch(Exception e)  {
//			response.flash = e
//		}
	
	}
	
	
	def void getProductInformation(ActionRequest request, ActionResponse response){
		
		InvoiceLine invoiceLine = request.context as InvoiceLine
		
		Invoice invoice = invoiceLine.invoice
		
		if(invoice == null)  {
			invoice = request.context.parentContext as Invoice
		}
			
		if(invoice != null && invoiceLine.product != null)  {
			
//			try  {
				boolean isPurchase = invoiceLineService.isPurchase(invoice)
				
				response.values = ["vatLine" : invoiceLineService.getVatLine(invoice, invoiceLine, isPurchase),
					"price" : invoiceLineService.getUnitPrice(invoice, invoiceLine, isPurchase),
					"productName" : invoiceLine?.product?.name]
//			}
//			catch(Exception e)  {
//				response.flash = e
//			}
		}
	}
	
	
	
}
