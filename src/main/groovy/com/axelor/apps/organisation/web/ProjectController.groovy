package com.axelor.apps.organisation.web

import com.axelor.apps.organisation.db.Project
import com.axelor.apps.organisation.service.ProjectService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class ProjectController {

	@Inject
	ProjectService projectService
	
	def createDefaultTask(ActionRequest request, ActionResponse response) {
		
		Project project = request.context as Project
		
		if(project) {
			
			projectService.createDefaultTask(project)
		}
	}
	
	def createPreSalesTask(ActionRequest request, ActionResponse response) {
		
		Project affair = request.context as Project
		
		if(affair) {
			
			projectService.createPreSalesTask(affair)
		}
	}
}
