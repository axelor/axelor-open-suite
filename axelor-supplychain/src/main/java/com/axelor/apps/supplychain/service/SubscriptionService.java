package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.db.Repository;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface SubscriptionService extends Repository<Subscription>{
	@Transactional
	public SaleOrderLine generateSubscriptions(SaleOrderLine saleOrderLine) throws AxelorException;
	
	@Transactional
	public Invoice generateInvoice(Subscription subscription,SaleOrderLine saleOrderLine,SaleOrder saleOrder) throws AxelorException;
	
	@Transactional
	public SaleOrderLine saveSaleOrderLine(SaleOrderLine saleOrderLine);
	
}
