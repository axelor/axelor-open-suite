package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Reimbursement
import com.axelor.apps.account.service.ReimbursementService
import com.axelor.apps.base.db.Status
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class ReimbursementController {
	
	@Inject
	private ReimbursementService rs
	
	def void validateReimbursement(ActionRequest request, ActionResponse response) {
		
		Reimbursement reimbursement = request.context as Reimbursement
		rs.updateContractCurrentRIB(reimbursement)
		
		if (reimbursement.bankDetails != null)  {
			
			response.values = [
				"status" : Status.all().filter("self.code = 'val'").fetchOne()
			]
			
		}
		else {
			
			response.flash = "Vous devez configurer un RIB"
			
		}
		
	}
}
