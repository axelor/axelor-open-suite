package com.axelor.apps.cash.management.web;

import com.axelor.apps.cash.management.db.ForecastGenerator;
import com.axelor.apps.cash.management.service.ForecastService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ForecastController {
	
	@Inject
	protected ForecastService forecastService;
	
	public void generate(ActionRequest request, ActionResponse response){
		ForecastGenerator forecastGenerator = request.getContext().asType(ForecastGenerator.class);
		forecastService.generate(forecastGenerator);
		response.setValues(forecastGenerator);
	}
	
}
