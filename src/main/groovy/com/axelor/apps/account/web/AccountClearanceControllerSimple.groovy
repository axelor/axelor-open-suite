package com.axelor.apps.account.web

import com.axelor.apps.account.db.MoveLine
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.rpc.Context

class AccountClearanceControllerSimple {
	
	def void showAccountClearanceMoveLines(ActionRequest request, ActionResponse response)  {
		
		Context context = request.context
		
		response.view = [
			title : "Lignes d'écriture générées",
			resource : MoveLine.class.name,
			domain : "self.accountClearance.id = ${context.id}"
		]
		
	}

}
