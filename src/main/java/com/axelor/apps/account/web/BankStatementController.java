package com.axelor.apps.account.web;

import com.axelor.apps.account.db.BankStatement;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class BankStatementController {

	@Inject
	private Provider<PeriodService> periodService;
	

	public void getPeriod(ActionRequest request, ActionResponse response) {
		
		BankStatement bankStatement = request.getContext().asType(BankStatement.class);
	
		try {
			if(bankStatement.getFromDate() != null && bankStatement.getCompany() != null) {
				
				response.setValue("period", periodService.get().rightPeriod(bankStatement.getFromDate(), bankStatement.getCompany()));				
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}

}
