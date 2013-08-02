package com.axelor.apps.account.web;

import com.axelor.apps.account.service.debtrecovery.PayerQualityService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class GeneralController {

	@Inject
	private Injector injector;
	
	public void payerQualityProcess(ActionRequest request, ActionResponse response)  {
		
		try  {
			PayerQualityService pqs = injector.getInstance(PayerQualityService.class);
			pqs.payerQualityProcess();
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}
}
