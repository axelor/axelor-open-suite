package com.axelor.apps.cash.management.web;

import com.axelor.apps.cash.management.db.CashFlowForecastGenerator;
import com.axelor.apps.cash.management.service.CashFlowForecastService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class CashFlowForecastController {
	
	@Inject
	protected CashFlowForecastService cashFlowForecastService;
	
	public void generate(ActionRequest request, ActionResponse response){
		CashFlowForecastGenerator cashFlowForecastGenerator = request.getContext().asType(CashFlowForecastGenerator.class);
		cashFlowForecastService.generate(cashFlowForecastGenerator);
		response.setValues(cashFlowForecastGenerator);
	}
	
}
