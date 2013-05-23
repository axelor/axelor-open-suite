package com.axelor.apps.account.service.invoice;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.account.service.CurrencyService;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class InvoiceLineService {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceLineService.class);
	
	
	@Inject
	private AccountManagementService accountManagementService;

	@Inject
	private CurrencyService currencyService;
	
	
	public VatLine getVatLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException  {
		
		return accountManagementService.getVatLine(invoice.getInvoiceDate(), invoiceLine.getProduct(), invoice.getCompany(), isPurchase);
		
	}
	
	
	public BigDecimal getUnitPrice(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException  {
		
		Product product = invoiceLine.getProduct();
		
		if(isPurchase)  {  
			return currencyService.getAmountCurrencyConverted(
				product.getPurchaseCurrency(), invoice.getCurrency(), product.getPurchasePrice(), invoice.getInvoiceDate());  
		}
		else  {  
			return currencyService.getAmountCurrencyConverted(
				product.getSaleCurrency(), invoice.getCurrency(), product.getSalePrice(), invoice.getInvoiceDate());  
		}
	}
	
	
	public boolean isPurchase(Invoice invoice)  {
		int operation = invoice.getOperationTypeSelect();
		if(operation == 1 || operation == 2)  { return true; }
		else  { return false; }
	}
	
	
	public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getPartner().getCurrency(), exTaxTotal, invoice.getInvoiceDate());  
	}
	
	
	
	
}
