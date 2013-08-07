package com.axelor.apps.base.web;

import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.formula.FormulaService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class GeneralController {

	@Inject
	private Injector injector;
	
	public void formulaClear(ActionRequest request, ActionResponse response){
		
		General context = request.getContext().asType(General.class);
		
		try {
			FormulaService.reset();
			response.setFlash("Jeu de formule réinitialisé avec les formules : "+context.getFormulaGenerator().getName());
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}
}
