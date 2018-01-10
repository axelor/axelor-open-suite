/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.web.project;

import java.math.BigDecimal;

import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.project.ProjectTaskService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectTaskController {

	@Inject
	private ProjectTaskService projectTaskService;
	
	@Inject
	private EmployeeService employeeService;
	
	
	public void setStoredDuration(ActionRequest request, ActionResponse response){
		response.setValue("duration", employeeService.getUserDuration(new BigDecimal(request.getContext().get("visibleDuration").toString()), AuthUtils.getUser(), true));
	}
	
	public void setStoredTimeSpent(ActionRequest request, ActionResponse response){
		response.setValue("timeSpent", employeeService.getUserDuration(new BigDecimal(request.getContext().get("visibleDuration").toString()), AuthUtils.getUser(), true));
	}
	
	public void setStoredLeadDelay(ActionRequest request, ActionResponse response){
		response.setValue("leadDelay", employeeService.getUserDuration(new BigDecimal(request.getContext().get("visibleDuration").toString()), AuthUtils.getUser(), true));
	}	
	
	public void setVisibleDuration(ActionRequest request, ActionResponse response){
		ProjectTask project = request.getContext().asType(ProjectTask.class);
		project = Beans.get(ProjectTaskRepository.class).find(project.getId());

		response.setValue("timesheetLineList", projectTaskService.computeVisibleDuration(project));
	}
	
	public void setProjectVisibleDuration(ActionRequest request, ActionResponse response){
		ProjectTask project = request.getContext().asType(ProjectTask.class);
		project = Beans.get(ProjectTaskRepository.class).find(project.getId());
		User user = AuthUtils.getUser();
		
		response.setValue("$visibleDuration", employeeService.getUserDuration(project.getDuration(), user, false));
		response.setValue("$visibleTimeSpent", employeeService.getUserDuration(project.getTimeSpent(), user, false));
		response.setValue("$visibleLeadDelay", employeeService.getUserDuration(project.getLeadDelay(), user, false));
	}
	
}
