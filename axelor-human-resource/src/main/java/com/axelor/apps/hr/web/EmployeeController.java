package com.axelor.apps.hr.web;

import com.axelor.apps.ReportFactory;
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
				.addModel(Beans.get(EmployeeRepository.class).find(Long.valueOf(employeeId)))
				.generate()
				.getFileLink();
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
		
		response.setCanClose(true);
	}
	

}
