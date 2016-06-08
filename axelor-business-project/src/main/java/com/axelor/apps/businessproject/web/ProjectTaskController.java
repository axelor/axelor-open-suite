/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.web;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.businessproject.report.IReport;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectTaskController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@Inject
	private ProjectTaskService projectTaskService;
	
	@Inject
	private GeneralService generalService;
	
	public void printProjectTask(ActionRequest request,ActionResponse response) throws AxelorException  {

		ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en";

		String name = I18n.get("Project Task") + " " + projectTask.getCode();
		
		String fileLink = ReportFactory.createReport(IReport.PROJECT_TASK, name+"-${date}")
				.addParam("ProjectTaskId", projectTask.getId())
				.addParam("Locale", language)
				.addModel(projectTask)
				.generate()
				.getFileLink();

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
	}

	public void computeProgress(ActionRequest request,ActionResponse response){

		ProjectTask projectTask = request.getContext().asType(ProjectTask.class);
		
		BigDecimal duration = BigDecimal.ZERO;
		if(BigDecimal.ZERO.compareTo(projectTask.getDuration()) != 0){
			duration = projectTask.getTimeSpent().add(projectTask.getLeadDelay()).divide(projectTask.getDuration(), 2, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100));
		}
		
		if(duration.compareTo(BigDecimal.ZERO) == -1 || duration.compareTo(new BigDecimal(100)) == 1){
			duration = BigDecimal.ZERO;
		}

		response.setValue("progress", duration);

	}

	public void computeDurationFromChildren(ActionRequest request, ActionResponse response)  {
		ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

		BigDecimal duration = projectTaskService.computeDurationFromChildren(projectTask.getId());

		BigDecimal visibleDuration = Beans.get(EmployeeService.class).getUserDuration(duration,generalService.getGeneral().getDailyWorkHours(),false);

		response.setValue("duration", duration);
		response.setValue("$visibleDuration", visibleDuration);

	}

}
