/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportSettings;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class EmployeeController {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showEmployee(ActionRequest request, ActionResponse response) {

		Employee employee = request.getContext().asType(Employee.class);

		StringBuilder url = new StringBuilder();
		
		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(
				new ReportSettings(IReport.EMPLOYEE)
				.addParam("Locale", language)
				.addParam("EmployeeId", employee.getId().toString())
				.getUrl());
		
		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations sur l'employe "+employee.getName() + " : "+url.toString());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Employee "+employee.getName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}

	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printCompanyPhonebook(ActionRequest request, ActionResponse response) {

		StringBuilder url = new StringBuilder();

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en";
		String attachmentPath = AppSettings.get().getPath("file.upload.dir","");
		attachmentPath = attachmentPath.endsWith("/") ? attachmentPath : attachmentPath+"/";

		url.append(new ReportSettings(IReport.COMPANY_PHONE_BOOK)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("UserId", user.getId().toString())
					.addParam("AttachmentPath",attachmentPath)
					.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			LOG.debug("Impression des informations sur le partenaire Company PhoneBook");

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Company PhoneBook");
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		   
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
}
