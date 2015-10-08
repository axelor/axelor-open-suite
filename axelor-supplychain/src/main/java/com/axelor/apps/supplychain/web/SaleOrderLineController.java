package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SaleOrderLineController {
	
	@Inject
	protected SaleOrderLineServiceSupplyChainImpl saleOrderLineServiceSupplyChainImpl;
	
	public void computeAnalyticDistribution(ActionRequest request, ActionResponse response){
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		if(Beans.get(GeneralService.class).getGeneral().getManageAnalyticAccounting()){
			saleOrderLine = saleOrderLineServiceSupplyChainImpl.computeAnalyticDistribution(saleOrderLine);
			response.setValue("analyticDistributionLineList", saleOrderLine.getAnalyticDistributionLineList());
		}
	}
}
