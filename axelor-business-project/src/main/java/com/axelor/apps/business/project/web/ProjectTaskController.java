package com.axelor.apps.business.project.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.business.project.report.IReport;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectTaskController {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectTaskController.class);

	@Inject
	private ProjectTaskService projectTaskService;

	public void printProjectTask(ActionRequest request,ActionResponse response){

		ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

		StringBuilder url = new StringBuilder();
		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en";

		url.append(new ReportSettings(IReport.PROJECT_TASK)
		.addParam("Locale", language)
		.addParam("__locale", "fr_FR")
		.addParam("ProjectTaskId", projectTask.getId().toString())
		.getUrl());

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());

		if (urlNotExist == null){
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", I18n.get("Project Task"));
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);
		}
		else {
			response.setFlash(urlNotExist);
		}
	}

	public void computeProgress(ActionRequest request,ActionResponse response){

		ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

		BigDecimal duration = projectTask.getTimeSpent().add(projectTask.getLeadDelay()).divide(projectTask.getDuration(), 2, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100));

		if(duration.compareTo(BigDecimal.ZERO) == -1 || duration.compareTo(new BigDecimal(100)) == 1){
			duration = BigDecimal.ZERO;
		}

		response.setValue("progress", duration);

	}

	public void computeDurationFromChildren(ActionRequest request, ActionResponse response){
		ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

		BigDecimal duration = projectTaskService.computeDurationFromChildren(projectTask.getId());

		BigDecimal visibleDuration = Beans.get(EmployeeService.class).getUserDuration(duration);

		response.setValue("duration", duration);
		response.setValue("$visibleDuration", visibleDuration);

	}

}
