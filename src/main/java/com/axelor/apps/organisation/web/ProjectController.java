package com.axelor.apps.organisation.web;

import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.service.ProjectService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectController {

	@Inject
	ProjectService projectService;
	
	public void createDefaultTask(ActionRequest request, ActionResponse response) {
		
		Project project = request.getContext().asType(Project.class);
		
		if(project != null) {			
			projectService.createDefaultTask(project);
		}
	}
	
	public void createPreSalesTask(ActionRequest request, ActionResponse response) {
		
		Project affair = request.getContext().asType(Project.class);
		
		if(affair != null) {			
			projectService.createPreSalesTask(affair);
		}
	}
	
}
