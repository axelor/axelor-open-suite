package com.axelor.apps.accountorganisation.web

import com.axelor.apps.account.db.Invoice
import com.axelor.apps.accountorganisation.service.TaskInvoiceService;
import com.axelor.apps.organisation.db.Project
import com.axelor.apps.organisation.db.Task
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class ProjectInvoiceController {

	@Inject
	private TaskInvoiceService taskInvoiceService
	
	def createInvoices(ActionRequest request, ActionResponse response) {
		
		Project project = request.context as Project
		int countInvoices = 0
		
		if(project) {
			
			if(project.getTaskList()) {
				
				for(Task task : project.getTaskList()) {
					
					try {						
						Invoice invoice = taskInvoiceService.generateInvoice(task)
						
						if(invoice)
							countInvoices++
					}
					catch(Exception e)  { TraceBackService.trace(response, e) }
				}
			}
			if(countInvoices > 0)  {
				response.reload = true
				response.flash = "${countInvoices} invoices have been created in draft state."
			}
			else {
				response.reload = true
				response.flash = "There is currently no tasks to invoice."
			}
		}
	}
}
