package com.axelor.apps.supplychain.web;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.OpportunitySaleOrderService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class OpportunitySaleOrderController{
	
	@Inject 
	private OpportunitySaleOrderService opportunitySaleOrderService;
	
	public void createClient(ActionRequest request, ActionResponse response) throws AxelorException{
		Opportunity opportunity = request.getContext().asType(Opportunity.class);
		opportunity = Beans.get(OpportunityRepository.class).find(opportunity.getId());
		opportunitySaleOrderService.createClientFromLead(opportunity);
		response.setReload(true);
	}
	
	public void generateSaleOrder(ActionRequest request, ActionResponse response) throws AxelorException{
		Opportunity opportunity = request.getContext().asType(Opportunity.class);
		opportunity = Beans.get(OpportunityRepository.class).find(opportunity.getId());
		SaleOrder saleOrder = opportunitySaleOrderService.createSaleOrderFromOpportunity(opportunity);
		response.setReload(true);
		response.setView(ActionView
				.define("SaleOrder")
				.model(SaleOrder.class.getName())
				.add("form", "sale-order-form")
				.context("_showRecord", String.valueOf(saleOrder.getId())).map());
	}
}
