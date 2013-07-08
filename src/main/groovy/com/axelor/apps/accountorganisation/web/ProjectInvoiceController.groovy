package com.axelor.apps.accountorganisation.web

import com.axelor.apps.accountorganisation.service.TaskInvoiceService;
import com.axelor.apps.organisation.db.Project
import com.axelor.apps.organisation.db.Task
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class ProjectInvoiceController {

	@Inject
	private TaskInvoiceService tis
	
	def createInvoices(ActionRequest request, ActionResponse response) {
		
		Project project = request.context as Project
		
		if(project) {
			
			if(project.getTaskList()) {
				
				for(Task task : project.getTaskList()) {
					
					tis.createInvoice(task)
				}
			}
		}
	}
}
