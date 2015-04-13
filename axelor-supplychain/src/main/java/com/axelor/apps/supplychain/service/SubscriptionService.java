package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.exception.AxelorException;

public interface SubscriptionService {
	public SaleOrderLine generateSubscriptions(SaleOrderLine saleOrderLine);
	
	public Invoice generateInvoice(Subscription subscription,SaleOrderLine saleOrderLine,SaleOrder saleOrder) throws AxelorException;
	
}
