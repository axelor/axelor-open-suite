package com.axelor.apps.base.web;

import com.axelor.apps.base.db.IndicatorGeneratorGrouping;
import com.axelor.apps.base.service.administration.IndicatorGeneratorGroupingService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class IndicatorGeneratorGroupingController {

	@Inject
	private IndicatorGeneratorGroupingService indicatorGeneratorGroupingService;
	
	public void run(ActionRequest request, ActionResponse response){
		
		IndicatorGeneratorGrouping indicatorGeneratorGrouping = request.getContext().asType(IndicatorGeneratorGrouping.class);
		indicatorGeneratorGrouping.find(indicatorGeneratorGrouping.getId());
		
		try {	
			indicatorGeneratorGroupingService.run(indicatorGeneratorGrouping);
			response.setReload(true);
			response.setFlash("Requête exécutée");		
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}
	
	public void export(ActionRequest request, ActionResponse response){
		
		IndicatorGeneratorGrouping indicatorGeneratorGrouping = request.getContext().asType(IndicatorGeneratorGrouping.class);
		indicatorGeneratorGrouping.find(indicatorGeneratorGrouping.getId());
		
		try {
			indicatorGeneratorGroupingService.export(indicatorGeneratorGrouping);
			response.setReload(true);
			response.setFlash("Resultat exporté");
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}
}
