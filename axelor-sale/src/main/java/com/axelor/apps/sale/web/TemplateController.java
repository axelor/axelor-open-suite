package com.axelor.apps.sale.web;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.TemplateService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TemplateController extends SaleOrderRepository{
	
	@Inject 
	protected TemplateService templateService;
	
	public void createSaleOrder(ActionRequest request, ActionResponse response)  {
		SaleOrder context = request.getContext().asType(SaleOrder.class);
		context = find(context.getId());
		SaleOrder copy = templateService.createSaleOrder(context);
		response.setView(ActionView
	            .define("Order")
	            .model(SaleOrder.class.getName())
	            .add("grid", "sale-order-grid")
	            .add("form", "sale-order-form")
	            .context("_showRecord", copy.getId().toString())
	            .map());
	}
	
	public void createTemplate(ActionRequest request, ActionResponse response)  {
		SaleOrder context = request.getContext().asType(SaleOrder.class);
		context = find(context.getId());
		SaleOrder copy = templateService.createTemplate(context);
		response.setView(ActionView
	            .define("Template")
	            .model(SaleOrder.class.getName())
	            .add("grid", "sale-order-template-grid")
	            .add("form", "sale-order-template-form")
	            .context("_showRecord", copy.getId().toString())
	            .map());
	}
}
