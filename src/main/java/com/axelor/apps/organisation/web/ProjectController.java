/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.service.ProjectService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectController {

	@Inject
	ProjectService projectService;
	
	public void createDefaultTask(ActionRequest request, ActionResponse response) {
		
		Project project = request.getContext().asType(Project.class);
		
		if(project.getId() != null && project.getDefaultTask() == null) {			
			projectService.createDefaultTask(Project.find(project.getId()));
			response.setReload(true);
		}
	}
	
	public void createPreSalesTask(ActionRequest request, ActionResponse response) {
		
		Project affair = request.getContext().asType(Project.class);
		
		if(projectService.createPreSalesTask(affair) != null)  {
			response.setReload(true);
		}
	}
	
	public void updateFinancialInformation(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Project project = request.getContext().asType(Project.class);
		
		if(project.getId() != null)  {
			projectService.updateFinancialInformation(project);
			
			response.setValue("initialEstimatedTurnover", project.getInitialEstimatedTurnover());
			response.setValue("initialEstimatedCost", project.getInitialEstimatedCost());
			response.setValue("initialEstimatedMargin", project.getInitialEstimatedMargin());
			response.setValue("realEstimatedTurnover", project.getRealEstimatedTurnover());
			response.setValue("realEstimatedCost", project.getRealEstimatedCost());
			response.setValue("realEstimatedMargin", project.getRealEstimatedMargin());
			response.setValue("realInvoicedTurnover", project.getRealInvoicedTurnover());
			response.setValue("realInvoicedCost", project.getRealInvoicedCost());
			response.setValue("realInvoicedMargin", project.getRealInvoicedMargin());
		}
	}
	
	
	public void updateTaskProgress(ActionRequest request, ActionResponse response) {
		
		Project project = request.getContext().asType(Project.class);
		
		projectService.updateTaskProgress(project);
	}
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printProjectReport(ActionRequest request, ActionResponse response) {

		Project project = request.getContext().asType(Project.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();

		MetaUser metaUser = MetaUser.findByUser( AuthUtils.getUser());
		
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Project.rptdesign&__format="+project.getExportTypeSelect()+"&Locale="+metaUser.getLanguage()+"&ProjectId="+project.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Name "+project.getAffairName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
