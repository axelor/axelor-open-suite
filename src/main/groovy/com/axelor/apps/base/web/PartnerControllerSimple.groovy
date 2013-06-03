package com.axelor.apps.base.web

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.base.db.Partner
import com.axelor.auth.db.User
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

import com.axelor.sn.service.LnService
import com.axelor.sn.service.SNFBService
import com.axelor.sn.service.SNTWTService
import com.google.inject.Inject;


class PartnerControllerSimple {
	@Inject
	SNFBService fbService;
	
	@Inject
	SNTWTService twtService;
	
	@Inject
	LnService linService;
	
	def void showInvoice(ActionRequest request, ActionResponse response)  {
		
		Partner partner = request.context as Partner
		
		response.view = [
			title : "Factures",
			resource : Invoice.class.name,
			domain : "self.payerPartner.id = ${partner.id} AND self.inTaxTotalRemaining != 0"
		]
		
	}
	
	void importFBContacts(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		response.flash = fbService.importContactsFBERP(user);
	}
	
	void importTWTContacts(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		response.flash = twtService.importContactsTWTERP(user);
	}
	
	void importLINContacts(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		response.flash = linService.importContactsLINERP(user);
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
