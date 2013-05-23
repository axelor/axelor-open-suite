package com.axelor.apps.base.web

import com.axelor.apps.base.db.IndicatorGenerator
import com.axelor.apps.base.service.administration.IndicatorGeneratorService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

class IndicatorGeneratorController {
	
	@Inject
	private IndicatorGeneratorService indicatorGeneratorService
	
	def void run(ActionRequest request, ActionResponse response){
		
		IndicatorGenerator indicatorGenerator = request.context as IndicatorGenerator
		indicatorGenerator.find(indicatorGenerator.getId());
		
		try {
			
			indicatorGeneratorService.run(indicatorGenerator)
			response.reload = true
			response.flash = "Requête exécutée"
			
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
		
	}
	
}
