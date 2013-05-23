package com.axelor.apps.base.web

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.base.db.Partner
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse


class PartnerControllerSimple {
	
	def void showInvoice(ActionRequest request, ActionResponse response)  {
		
		Partner partner = request.context as Partner
		
		response.view = [
			title : "Factures",
			resource : Invoice.class.name,
			domain : "self.payerPartner.id = ${partner.id} AND self.inTaxTotalRemaining != 0"
		]
		
	}
	
	
//	def showActionEvent(ActionRequest request, ActionResponse response) {
//		
//	   Partner partner = request.context as Partner
//	
//	   response.view = [
//		   title : "Evènements : ${partner.mainContact?.name}",
//		   resource : ActionEvent.class.name,
//		   domain : "self.base.id = ${partner.id}"
//	   ]
//	
//   }
	
	
}
