package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AccountingSituationController {

	@Inject
	private AccountCustomerService acs;
	
	public void updateCustomerAccount(ActionRequest request, ActionResponse response)  {
		
		AccountingSituation accountingSituation = request.getContext().asType(AccountingSituation.class);
		accountingSituation = AccountingSituation.find(accountingSituation.getId());
		
		try {
			if(accountingSituation != null)  {
				acs.updateMoveLineSet(accountingSituation);
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
