package com.axelor.apps.base.web

import com.axelor.apps.base.db.General
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector

class GeneralController {
	
	@Inject
	private Injector injector
	
	
	def void formulaClear(ActionRequest request, ActionResponse response){
		
		General context = request.context as General
		
		try {
			
			FormulaService.reset()
			response.flash = "Jeu de formule réinitialisé avec les formules : ${context.formulaGenerator.name}"
			
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
		
	}
}
