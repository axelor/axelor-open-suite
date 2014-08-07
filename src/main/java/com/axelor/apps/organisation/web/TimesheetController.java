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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.report.IReport;
import com.axelor.apps.organisation.service.TimesheetService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
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
		Company company = timesheet.getUser().getActiveCompany();
		
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
		String language = timesheet.getUser().getPartner().getLanguageSelect() != null? timesheet.getUser().getPartner().getLanguageSelect() : timesheet.getUser().getActiveCompany().getPrintingSettings().getLanguageSelect() != null ? timesheet.getUser().getActiveCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		language = language.equals("")? "en": language;
		
		url.append(
				new ReportSettings(IReport.TIMESHEET)
				.addParam("Locale", language)
				.addParam("TimesheetId", timesheet.getId().toString())
				.getUrl());
		
		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations timesheet "+timesheet.getUser().getPartner().getName()+" "+timesheet.getUser().getPartner().getFirstName()+" : "+url.toString());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Employee "+timesheet.getUser().getPartner().getName()+" "+timesheet.getUser().getPartner().getFirstName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
