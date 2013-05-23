package com.axelor.apps.base.web

import com.axelor.apps.base.db.IndicatorGeneratorGrouping
import com.axelor.apps.base.service.administration.IndicatorGeneratorGroupingService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

class IndicatorGeneratorGroupingController {
	
	@Inject
	private IndicatorGeneratorGroupingService indicatorGeneratorGroupingService
	
	def void run(ActionRequest request, ActionResponse response){
		
		IndicatorGeneratorGrouping indicatorGeneratorGrouping = request.context as IndicatorGeneratorGrouping
		indicatorGeneratorGrouping.find(indicatorGeneratorGrouping.getId());
		
		try {
			
			indicatorGeneratorGroupingService.run(indicatorGeneratorGrouping)
			response.reload = true
			response.flash = "Requête exécutée"
			
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
		
	}
	
	
	def void export(ActionRequest request, ActionResponse response){
		
		IndicatorGeneratorGrouping indicatorGeneratorGrouping = request.context as IndicatorGeneratorGrouping
		indicatorGeneratorGrouping.find(indicatorGeneratorGrouping.getId());
		
		try {
			
			indicatorGeneratorGroupingService.export(indicatorGeneratorGrouping)
			response.reload = true
			response.flash = "Resultat exporté"
			
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
		
	}
	
}
