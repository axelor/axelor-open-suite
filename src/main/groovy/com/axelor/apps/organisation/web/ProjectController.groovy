package com.axelor.apps.organisation.web

import com.axelor.apps.organisation.db.Project
import com.axelor.apps.organisation.db.Task
import com.axelor.apps.organisation.service.TaskService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class ProjectController {
	
	@Inject
	private TaskService taskservice
	
	def createInvoices(ActionRequest request, ActionResponse response) {
		
		Project project = request.context as Project
		
		if(project) {
			
			if(project.getTaskList()) {
				
				for(Task task : project.getTaskList()) {
					
					taskservice.createInvoice(task)
				}
			}
		}	
	}
	
}
