/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.service.TimesheetService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TimesheetController {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);
	
	@Inject
	private Provider<PeriodService> periodService;
	
	@Inject
	private Provider<TimesheetService> timesheetService;
	
	public void getPeriod(ActionRequest request, ActionResponse response) {

		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		Company company = timesheet.getUserInfo().getActiveCompany();
		
		try {
			
			if(timesheet.getFromDate() != null && company != null)  {

				response.setValue("period", periodService.get().rightPeriod(timesheet.getFromDate(), company));
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void getTaskSpentTime(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		
		timesheetService.get().getTaskSpentTime(timesheet);
		
		response.setReload(true);
		
	}
	
	public void validate(ActionRequest request, ActionResponse response) {
		
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		
		timesheetService.get().validate(timesheet);
		
		response.setReload(true);
	}
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printTimesheet(ActionRequest request, ActionResponse response) {

		Timesheet timesheet = request.getContext().asType(Timesheet.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();
		String language = timesheet.getUserInfo().getPartner().getLanguageSelect() != null? timesheet.getUserInfo().getPartner().getLanguageSelect() : timesheet.getUserInfo().getActiveCompany().getPrintingSettings().getLanguageSelect() != null ? timesheet.getUserInfo().getActiveCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		language = language.equals("")? "en": language;
		
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Timesheet.rptdesign&__format=pdf&TimesheetId="+timesheet.getId()+"&Locale="+language+axelorSettings.get("axelor.report.engine.datasource"));

		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations timesheet "+timesheet.getUserInfo().getPartner().getName()+" "+timesheet.getUserInfo().getPartner().getFirstName()+" : "+url.toString());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Employee "+timesheet.getUserInfo().getPartner().getName()+" "+timesheet.getUserInfo().getPartner().getFirstName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
