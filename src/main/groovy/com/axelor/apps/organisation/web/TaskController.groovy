package com.axelor.apps.organisation.web

import com.axelor.apps.organisation.db.Task
import com.axelor.apps.organisation.service.TaskService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class TaskController {

	@Inject
	private TaskService taskservice
	
	def createInvoice(ActionRequest request, ActionResponse response) {
		
		Task task = request.context as Task
		
		if(task) {
			taskservice.createInvoice(task)
		}
	}
	
}
