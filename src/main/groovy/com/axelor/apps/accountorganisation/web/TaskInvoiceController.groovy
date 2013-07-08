package com.axelor.apps.accountorganisation.web

import com.axelor.apps.accountorganisation.service.TaskInvoiceService
import com.axelor.apps.organisation.db.Task
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

class TaskInvoiceController {
	
	@Inject
	private TaskInvoiceService tis
	
	def createInvoice(ActionRequest request, ActionResponse response) {
		
		Task task = request.context as Task
		
		if(task) {
			tis.createInvoice(task)
		}
	}
}
