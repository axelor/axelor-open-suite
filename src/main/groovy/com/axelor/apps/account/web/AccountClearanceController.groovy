package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.AccountClearance
import com.axelor.apps.account.service.AccountClearanceService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class AccountClearanceController {
	
	@Inject
	private AccountClearanceService acs
	
	def void getExcessPayment(ActionRequest request, ActionResponse response)  {
		
		AccountClearance accountClearance = request.context as AccountClearance
		
		try {
			
			acs.setExcessPayment(accountClearance)
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
	
	def void validateAccountClearance(ActionRequest request, ActionResponse response)  {
		
		AccountClearance accountClearance = request.context as AccountClearance
		accountClearance = AccountClearance.find(accountClearance.id)
		
		try {
			
			acs.validateAccountClearance(accountClearance)
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
}
