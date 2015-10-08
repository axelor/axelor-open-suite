package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderLineController {
	
	@Inject
	protected PurchaseOrderLineServiceSupplychainImpl purchaseOrderLineServiceSupplychainImpl;
	
	public void computeAnalyticDistribution(ActionRequest request, ActionResponse response){
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		if(Beans.get(GeneralService.class).getGeneral().getManageAnalyticAccounting()){
			purchaseOrderLine = purchaseOrderLineServiceSupplychainImpl.computeAnalyticDistribution(purchaseOrderLine);
			response.setValue("analyticDistributionLineList", purchaseOrderLine.getAnalyticDistributionLineList());
		}
	}
}
