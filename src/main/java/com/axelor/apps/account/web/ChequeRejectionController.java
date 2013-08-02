package com.axelor.apps.account.web;

import com.axelor.apps.account.db.ChequeRejection;
import com.axelor.apps.account.service.ChequeRejectionService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ChequeRejectionController {

	@Inject
	private ChequeRejectionService crs;
	
	public void validateChequeRejection(ActionRequest request, ActionResponse response)  {
		
		ChequeRejection chequeRejection = request.getContext().asType(ChequeRejection.class);
		chequeRejection = ChequeRejection.find(chequeRejection.getId());
		
		try {
			crs.validateChequeRejection(chequeRejection);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }		
	}
}
