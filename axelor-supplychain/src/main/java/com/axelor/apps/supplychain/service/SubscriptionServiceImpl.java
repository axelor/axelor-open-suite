package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SubscriptionServiceImpl implements SubscriptionService{
	
	@Inject
	protected SaleOrderInvoiceServiceImpl saleOrderInvoiceServiceImpl;
	
	public SaleOrderLine generateSubscriptions(SaleOrderLine saleOrderLine){
		int iterator = 0;
		while(iterator != saleOrderLine.getPeriodNumber()){
			Subscription subscription = new Subscription();
			if(saleOrderLine.getInvoicingTypeSelect() == 1){
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			}
			else{
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)));
			}
			subscription.setFromPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			subscription.setToPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)));
			subscription.setQuantity(saleOrderLine.getQty());
			subscription.setAmount(saleOrderLine.getPrice().multiply(new BigDecimal(saleOrderLine.getPeriodicity())));
			subscription.setInvoiced(false);
			saleOrderLine.addSubscriptionListItem(subscription);
			iterator++;
		}
		return saleOrderLine;
	}
	
	public Invoice generateInvoice(Subscription subscription,SaleOrderLine saleOrderLine ,SaleOrder saleOrder) throws AxelorException{
		
		InvoiceGenerator invoiceGenerator = saleOrderInvoiceServiceImpl.createInvoiceGenerator(saleOrder);
		
		Invoice invoice = invoiceGenerator.generate();
		
		return invoice;
	}
	
}
