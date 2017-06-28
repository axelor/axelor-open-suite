/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web;

import java.util.List;
import java.util.stream.Collectors;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.ModelService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.report.IReport;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import wslite.json.JSONException;
import wslite.json.JSONObject;

public class EmployeeController {
	
	
	public void showAnnualReport(ActionRequest request, ActionResponse response) throws JSONException, NumberFormatException, AxelorException{
		
		String employeeId = request.getContext().get("_id").toString();
		String year = request.getContext().get("year").toString();
		int yearId = new JSONObject(year).getInt("id");
		String yearName = new JSONObject(year).getString("name");
		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 
		
		String name = I18n.get("Annual report") + " :  " + user.getFullName() + " (" + yearName + ")";
		
		String fileLink = ReportFactory.createReport(IReport.EMPLOYEE_ANNUAL_REPORT, name)
				.addParam("EmployeeId", Long.valueOf(employeeId) )
				.addParam("YearId",  Long.valueOf(yearId) )
				.addParam("Locale", language)
				.toAttach(Beans.get(EmployeeRepository.class).find(Long.valueOf(employeeId)))
				.generate()
				.getFileLink();
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
		
		response.setCanClose(true);
	}

	public void massArchive(ActionRequest request, ActionResponse response) {
		@SuppressWarnings("unchecked")
		List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
		if (idList == null) {
			return;
		}
		EmployeeRepository employeeRepo = Beans.get(EmployeeRepository.class);
		List<Employee> employeeList = idList.stream().map(id -> employeeRepo.find(id.longValue()))
				.collect(Collectors.toList());
		Beans.get(ModelService.class).massArchive(employeeList);
		response.setReload(true);
	}

	public void massUnarchive(ActionRequest request, ActionResponse response) {
		@SuppressWarnings("unchecked")
		List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
		if (idList == null) {
			return;
		}
		EmployeeRepository employeeRepo = Beans.get(EmployeeRepository.class);
		List<Employee> employeeList = idList.stream().map(id -> employeeRepo.find(id.longValue()))
				.collect(Collectors.toList());
		Beans.get(ModelService.class).massUnarchive(employeeList);
		response.setReload(true);
	}

}
