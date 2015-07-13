package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SubscriptionService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SubscriptionController {

	@Inject
	protected SubscriptionService subscriptionService;

	@Inject
	protected SaleOrderLineRepository saleOrderLineRepo;

	public void generateSubscriptions(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

		if(saleOrderLine.getId()!=null && saleOrderLine.getId()>0){
			saleOrderLine=Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
		}

		saleOrderLine = subscriptionService.generateSubscriptions(saleOrderLine);
		response.setValue("subscriptionList", saleOrderLine.getSubscriptionList());
	}

	public void generateAllSubscriptions(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		saleOrder  = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

		for (SaleOrderLine saleOrderLineIt : saleOrder.getSaleOrderLineList()) {
			if(saleOrderLineIt.getProduct().getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE)){
				subscriptionService.generateSubscriptions(saleOrderLineIt,saleOrder);
			}
		}

		response.setReload(true);
	}

	public void generateInvoice(ActionRequest request, ActionResponse response)  throws AxelorException{

		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

		saleOrderLine = saleOrderLineRepo.find(saleOrderLine.getId());

		if (saleOrderLine != null){

			Invoice invoice = Beans.get(SaleOrderInvoiceService.class).generateSubcriptionsForSaleOrderLine(saleOrderLine);

			if(invoice == null){
				throw new AxelorException(I18n.get("No Subscription to Invoice"), IException.CONFIGURATION_ERROR);
			}
			response.setCanClose(true);
			response.setView(ActionView
		            .define(I18n.get("Invoice Generated"))
		            .model(Invoice.class.getName())
		            .add("form", "invoice-form")
		            .add("grid", "invoice-grid")
		            .context("_showRecord",String.valueOf(invoice.getId()))
		            .map());
		}
	}
}
