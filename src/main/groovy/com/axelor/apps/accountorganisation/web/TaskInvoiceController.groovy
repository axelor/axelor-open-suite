package com.axelor.apps.accountorganisation.web

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.accountorganisation.service.TaskInvoiceService
import com.axelor.apps.organisation.db.Task
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

class TaskInvoiceController {
	
	@Inject
	private TaskInvoiceService taskInvoiceService
	
	def createInvoice(ActionRequest request, ActionResponse response) {
		
		Task task = request.context as Task
		
		try {
			
			task = Task.find(task.getId())
			
			Invoice invoice = taskInvoiceService.generateInvoice(task)
			
			if(invoice != null)  {
				response.reload = true
				response.flash = "Facture créée"
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
}
