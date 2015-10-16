package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AnalyticDistributionLineController {
	
	@Inject
	protected AnalyticDistributionLineService analyticDistributionLineService;
	
	public void computeAmount(ActionRequest request, ActionResponse response){
		AnalyticDistributionLine analyticDistributionLine = request.getContext().asType(AnalyticDistributionLine.class);
		response.setValue("amount", analyticDistributionLineService.chooseComputeWay(request.getContext(), analyticDistributionLine));
	}
}
