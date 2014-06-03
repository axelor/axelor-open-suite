/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.organisation.db.ITask;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.report.IReport;
import com.axelor.apps.organisation.service.FinancialInformationHistoryLineService;
import com.axelor.apps.organisation.service.FinancialInformationHistoryService;
import com.axelor.apps.organisation.service.TaskService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TaskController {

	@Inject
	private Provider<TaskService> taskService;
	
	@Inject
	private Provider<FinancialInformationHistoryService> financialInformationHistoryService;
	
	@Inject
	private Provider<FinancialInformationHistoryLineService> financialInformationHistoryLineService;
	
	public void updateFinancialInformation(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
			taskService.get().updateFinancialInformation(task);
			
			// Les montants sont figés dès le commencement de la tache
			if(task.getStatusSelect() < ITask.STATUS_STARTED && task.getRealEstimatedMethodSelect() != ITask.REAL_ESTIMATED_METHOD_NONE)  {
				response.setValue("initialEstimatedTurnover", task.getInitialEstimatedTurnover());
				response.setValue("initialEstimatedCost", task.getInitialEstimatedCost());
				response.setValue("initialEstimatedMargin", task.getInitialEstimatedMargin());
			}
			response.setValue("realEstimatedTurnover", task.getRealEstimatedTurnover());
			response.setValue("realEstimatedCost", task.getRealEstimatedCost());
			response.setValue("realEstimatedMargin", task.getRealEstimatedMargin());
			response.setValue("realInvoicedTurnover", task.getRealInvoicedTurnover());
			response.setValue("realInvoicedCost", task.getRealInvoicedCost());
			response.setValue("realInvoicedMargin", task.getRealInvoicedMargin());
		}
	}
	
	
	public void updateFinancialInformationInitialEstimated(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
			
			financialInformationHistoryService.get().updateFinancialInformationInitialEstimatedHistory(task);
			
			taskService.get().updateInitialEstimatedAmount(task);
			
			response.setValue("initialEstimatedTurnover", task.getInitialEstimatedTurnover());
			response.setValue("initialEstimatedCost", task.getInitialEstimatedCost());
			response.setValue("initialEstimatedMargin", task.getInitialEstimatedMargin());
			response.setValue("financialInformationHistoryLineList", task.getFinancialInformationHistoryLineList());
		}
	}
	
	
	
	public void getSpentTime(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
				response.setValue("spentTime", taskService.get().getSpentTime(task));
		}
	}
	
	
	public void getPlannedTime(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Task task = request.getContext().asType(Task.class);
		
		if(task.getId() != null)  {
				response.setValue("plannedTime", taskService.get().getPlannedTime(task));
		}
	}


	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printTaskReport(ActionRequest request, ActionResponse response) {

		Task task = request.getContext().asType(Task.class);

		User user = AuthUtils.getUser();

		StringBuilder url = new StringBuilder();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(
				new ReportSettings(IReport.TASK, task.getExportTypeSelect())
				.addParam("Local", language)
				.addParam("__locale", "fr_FR")
				.addParam("TaskId", task.getId().toString())
				.getUrl());
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Name "+task.getName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	
}
