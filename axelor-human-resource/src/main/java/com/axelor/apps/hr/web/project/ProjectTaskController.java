package com.axelor.apps.hr.web.project;

import java.math.BigDecimal;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.service.employee.EmployeeService;
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
	
	@Inject
	private EmployeeService employeeService;
	
	@Inject
	private GeneralService generalService;
	
	public void setStoredDuration(ActionRequest request, ActionResponse response){
		response.setValue("duration", employeeService.getUserDuration(new BigDecimal(request.getContext().get("visibleDuration").toString()),generalService.getGeneral().getDailyWorkHours(), true));
	}
	
	public void setStoredTimeSpent(ActionRequest request, ActionResponse response){
		response.setValue("timeSpent", employeeService.getUserDuration(new BigDecimal(request.getContext().get("visibleDuration").toString()),generalService.getGeneral().getDailyWorkHours(), true));
	}
	
	public void setStoredLeadDelay(ActionRequest request, ActionResponse response){
		response.setValue("leadDelay", employeeService.getUserDuration(new BigDecimal(request.getContext().get("visibleDuration").toString()),generalService.getGeneral().getDailyWorkHours(), true));
	}	
	
	public void setVisibleDuration(ActionRequest request, ActionResponse response){
		ProjectTask project = request.getContext().asType(ProjectTask.class);
		project = Beans.get(ProjectTaskRepository.class).find(project.getId());

		response.setValue("timesheetLineList", projectTaskService.computeVisibleDuration(project));
	}
	
	public void setProjectVisibleDuration(ActionRequest request, ActionResponse response){
		ProjectTask project = request.getContext().asType(ProjectTask.class);
		project = Beans.get(ProjectTaskRepository.class).find(project.getId());
		
		response.setValue("$visibleDuration", employeeService.getUserDuration(project.getDuration(),generalService.getGeneral().getDailyWorkHours(), false));
		response.setValue("$visibleTimeSpent", employeeService.getUserDuration(project.getTimeSpent(),generalService.getGeneral().getDailyWorkHours(), false));
		response.setValue("$visibleLeadDelay", employeeService.getUserDuration(project.getLeadDelay(),generalService.getGeneral().getDailyWorkHours(), false));
	}
	
}
