package com.axelor.apps.account.web

import com.axelor.apps.account.service.debtrecovery.PayerQualityService
import com.axelor.apps.base.db.General
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector

class GeneralController {
	
	@Inject
	private Injector injector
	
	def void payerQualityProcess(ActionRequest request, ActionResponse response)  {
		
		try  {
			
			PayerQualityService pqs = injector.getInstance(PayerQualityService.class)
			
			pqs.payerQualityProcess()
			
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}
	
}
