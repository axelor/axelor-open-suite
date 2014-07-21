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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.report.IReport;
import com.axelor.apps.organisation.service.ProjectService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ProjectController {

	@Inject
	ProjectService projectService;
	
	private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

	public void createDefaultTask(ActionRequest request, ActionResponse response) {
		
		Project project = request.getContext().asType(Project.class);
		
		if(project.getId() != null && project.getDefaultTask() == null) {			
			projectService.createDefaultTask(Project.find(project.getId()));
			response.setReload(true);
		}
	}
	
	public void createPreSalesTask(ActionRequest request, ActionResponse response) {
		
		Project project = request.getContext().asType(Project.class);
		
		if(projectService.createPreSalesTask(project) != null)  {
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

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(
				new ReportSettings(IReport.PROJECT, project.getExportTypeSelect())
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("ProjectId", project.getId().toString())
				.getUrl());
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Name "+project.getName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	/**
	 * Fonction appeler par le Situation d'affaires
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printBusinessSituation(ActionRequest request, ActionResponse response) {


		Project business = request.getContext().asType(Project.class );
		String businessIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedBusiness = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedBusiness != null){
			for(Integer it : lstSelectedBusiness) {
				businessIds+= it.toString()+",";
			}
		}	
			
		if(!businessIds.equals("")){
			businessIds = businessIds.substring(0, businessIds.length()-1);	
			business = Project.find(new Long(lstSelectedBusiness.get(0)));
		}else if(business.getId() != null){
			businessIds = business.getId().toString();			
		}
		
		if(!businessIds.equals("")){
			StringBuilder url = new StringBuilder();			
			
			User user = AuthUtils.getUser();
			Company company = business.getCompany();
			
			String language = "en";
			if(user != null && !Strings.isNullOrEmpty(user.getLanguage()))  {
				language = user.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && !Strings.isNullOrEmpty(company.getPrintingSettings().getLanguageSelect())) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			url.append(
					new ReportSettings(IReport.BUSINESS_SITUATION)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("BusinessId", businessIds)
					.getUrl());
			
			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression de l'O.F.  "+business.getName()+" : "+url.toString());
				
				String title = " ";
				if(business.getName() != null)  {
					title += lstSelectedBusiness == null ? " Business "+business.getName():" Business ";
				}
				
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", title);
				mapView.put("resource", url);
				mapView.put("viewType", "html");
				response.setView(mapView);	
					
			}
			else {
				response.setFlash(urlNotExist);
			}
		}else{
			response.setFlash("Please select the Business(s) to print.");
		}	
	}
	
}
