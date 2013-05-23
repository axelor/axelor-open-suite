package com.axelor.apps.account.web

import com.axelor.apps.account.db.ChequeRejection
import com.axelor.apps.account.service.ChequeRejectionService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


class ChequeRejectionController {
	
	@Inject
	private ChequeRejectionService crs
	
	def void validateChequeRejection(ActionRequest request, ActionResponse response)  {
		
		ChequeRejection chequeRejection = request.context as ChequeRejection
		chequeRejection = ChequeRejection.find(chequeRejection.id)
		
		try {
			crs.validateChequeRejection(chequeRejection)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
}
