package com.axelor.apps.production.web;

import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.service.ProdProcessService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProdProcessController {
	
	@Inject
	protected ProdProcessService prodProcessService;
	
	public void validateProdProcess(ActionRequest request, ActionResponse response) throws AxelorException{
		ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
		if(prodProcess.getIsConsProOnOperation()){
			prodProcessService.validateProdProcess(prodProcess);
		}
	}
}
