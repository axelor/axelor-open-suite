package com.axelor.apps.supplychain.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SubscriptionService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SubscriptionController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderInvoiceServiceImpl.class); 
	
	@Inject
	protected SubscriptionService subscriptionService;
	
	public void generateSubscriptions(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		saleOrderLine = subscriptionService.generateSubscriptions(saleOrderLine);
		if(saleOrderLine.getId()>0){
			response.setReload(true);
		}
		else{
			response.setValue("subscriptionList", saleOrderLine.getSubscriptionList());
		}
	}
	
	public void generateAllSubscriptions(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		
		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		
		if(saleOrder == null){
			saleOrder = request.getContext().getParentContext().asType(SaleOrder.class);
		}
		
		saleOrder  = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
		
		for (SaleOrderLine saleOrderLineIt : saleOrder.getSaleOrderLineList()) {
			subscriptionService.generateSubscriptions(saleOrderLineIt,saleOrderLine);
		}
		
		response.setReload(true);
	}
	
	public void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		Subscription subscription = request.getContext().asType(Subscription.class);
		
		if(subscriptionService.find(subscription.getId())!=null){
			subscription=subscriptionService.find(subscription.getId());
		}		
		
		SaleOrderLine saleOrderLine = subscription.getSaleOrderLine();
		
		saleOrderLine  = Beans.get(SaleOrderLineService.class).find(saleOrderLine.getId());
		
		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		
		saleOrder  = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
		
		if(saleOrder == null){
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
