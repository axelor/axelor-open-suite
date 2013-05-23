package com.axelor.apps.account.web

import com.axelor.apps.base.db.Partner
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse


class ReminderControllerSimple {
	
	def showReminderMail(ActionRequest request, ActionResponse response) {
		
		Partner partner = request.context as Partner
		
		response.view = [
			title : "Courriers",
			resource : Mail.class.name,
			domain : "self.base.id = ${partner.id} AND self.typeSelect = 1",
		]
	
	}
	
	def showReminderEmail(ActionRequest request, ActionResponse response) {
		
		Partner partner = request.context as Partner
		
		response.view = [
			title : "Emails",
			resource : Mail.class.name,
			domain : "self.base.id = ${partner.id} AND self.typeSelect = 0",
		]
	
	}

}
