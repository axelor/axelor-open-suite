package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.SubscriptionService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SubscriptionController {
	
	@Inject
	protected SubscriptionService subscriptionService;
	
	public void generateSubscriptions(ActionRequest request, ActionResponse response){
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		saleOrderLine = subscriptionService.generateSubscriptions(saleOrderLine);
		response.setValue("subscriptionList", saleOrderLine.getSubscriptionList());
	}
	
	public void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		Subscription subscription = request.getContext().asType(Subscription.class);
		
		SaleOrderLine saleOrderLine = new SaleOrderLine();
		
		if(subscription.getSaleOrderLine()!=null){
			saleOrderLine = subscription.getSaleOrderLine();
		}
		else{
			saleOrderLine = request.getContext().getParentContext().asType(SaleOrderLine.class);
		}
		
		SaleOrder saleOrder = new SaleOrder();
		
		if(saleOrderLine.getSaleOrder()!=null){
			saleOrder = saleOrderLine.getSaleOrder();
		}
		else{
			saleOrder = request.getContext().getParentContext().getParentContext().asType(SaleOrder.class);
		}
		
		try {
			Invoice invoice = subscriptionService.generateInvoice(subscription,saleOrderLine,saleOrder);
			
			if(invoice != null)  {
				response.setReload(true);
				response.setFlash(I18n.get(IExceptionMessage.PO_INVOICE_2));
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
