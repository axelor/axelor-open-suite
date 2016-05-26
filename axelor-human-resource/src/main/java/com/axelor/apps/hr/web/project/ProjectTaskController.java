package com.axelor.apps.hr.web.project;

import com.axelor.apps.hr.service.project.ProjectTaskService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectTaskController {

	@Inject
	private ProjectTaskService projectTaskService;
	
	public void setVisibleDuration(ActionRequest request, ActionResponse response){
		ProjectTask project = request.getContext().asType(ProjectTask.class);
		project = Beans.get(ProjectTaskRepository.class).find(project.getId());

		response.setValue("timesheetLineList", projectTaskService.computeVisibleDuration(project));
	}
	
}
