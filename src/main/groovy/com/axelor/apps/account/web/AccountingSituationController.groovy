package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.AccountClearance
import com.axelor.apps.account.db.AccountingSituation
import com.axelor.apps.account.service.AccountCustomerService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class AccountingSituationController {
	
	@Inject
	private AccountCustomerService acs
	
	
	def void updateCustomerAccount(ActionRequest request, ActionResponse response)  {
		
		AccountingSituation accountingSituation = request.context as AccountingSituation
		accountingSituation = AccountingSituation.find(accountingSituation.id)
		
		try {
			if(accountingSituation != null)  {
				response.values = acs.updateMoveLineSet(accountingSituation)
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
}
