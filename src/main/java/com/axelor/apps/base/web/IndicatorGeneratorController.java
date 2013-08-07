package com.axelor.apps.base.web;

import com.axelor.apps.base.db.IndicatorGenerator;
import com.axelor.apps.base.service.administration.IndicatorGeneratorService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class IndicatorGeneratorController {

	@Inject
	private IndicatorGeneratorService indicatorGeneratorService;
	
	public void run(ActionRequest request, ActionResponse response){
		
		IndicatorGenerator indicatorGenerator = request.getContext().asType(IndicatorGenerator.class);
		indicatorGenerator.find(indicatorGenerator.getId());
		
		try {
			indicatorGeneratorService.run(indicatorGenerator);
			response.setReload(true);
			response.setFlash("Requête exécutée");
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}
}
