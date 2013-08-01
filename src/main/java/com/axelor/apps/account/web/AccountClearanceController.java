package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountClearance;
import com.axelor.apps.account.service.AccountClearanceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AccountClearanceController {

	@Inject
	private AccountClearanceService acs;
	
	public void getExcessPayment(ActionRequest request, ActionResponse response)  {
		
		AccountClearance accountClearance = request.getContext().asType(AccountClearance.class);
		
		try {	
			acs.setExcessPayment(accountClearance);
			response.setReload(true);		
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }		
	}
	
	public void validateAccountClearance(ActionRequest request, ActionResponse response)  {
		
		AccountClearance accountClearance = request.getContext().asType(AccountClearance.class);
		accountClearance = AccountClearance.find(accountClearance.getId());
		
		try {
			acs.validateAccountClearance(accountClearance);
			response.setReload(true);		
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
