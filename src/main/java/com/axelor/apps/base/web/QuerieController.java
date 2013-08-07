package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Querie;
import com.axelor.apps.base.service.querie.QuerieService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class QuerieController {
	
	@Inject
	private QuerieService qs;
	
	public void checkQuerie(ActionRequest request, ActionResponse response){
		
		try {
			qs.checkQuerie(request.getContext().asType(Querie.class));
			response.setFlash("Valid query.");
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}

}
