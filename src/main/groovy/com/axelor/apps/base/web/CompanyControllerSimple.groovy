package com.axelor.apps.base.web

import com.axelor.apps.account.db.Invoice
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

class CompanyControllerSimple {
	
	def showInvoice(ActionRequest request, ActionResponse response) {
		
		def context = request.context
		
		response.view = [
			title : "Factures",
			resource : Invoice.class.name,
			domain : "self.company.id = ${context.id}"
		]
	
	}
	
}
