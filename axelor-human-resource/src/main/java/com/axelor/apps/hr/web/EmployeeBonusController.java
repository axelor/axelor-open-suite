package com.axelor.apps.hr.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.hr.db.EmployeeBonusMgt;
import com.axelor.apps.hr.db.repo.EmployeeBonusMgtRepository;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.hr.service.EmployeeBonusService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EmployeeBonusController {
	
	
	@Inject
	EmployeeBonusMgtRepository employeeBonusMgtRepo;
	
	@Inject
	EmployeeBonusService employeeBonusService;
	

	public void compute(ActionRequest request, ActionResponse response) throws AxelorException{
		
		employeeBonusService.compute(employeeBonusMgtRepo.find( request.getContext().asType( EmployeeBonusMgt.class ).getId() ));
		response.setReload(true);
	}
	
	public void print(ActionRequest request, ActionResponse response) throws AxelorException{
		
		EmployeeBonusMgt bonus = employeeBonusMgtRepo.find( request.getContext().asType( EmployeeBonusMgt.class ).getId());
		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 
		
		String name = I18n.get("Employee bonus management") + " :  " + bonus.getEmployeeBonusType().getLabel();
		
		String fileLink = ReportFactory.createReport(IReport.EMPLOYEE_BONUS_MANAGEMENT, name)
				.addParam("EmployeeBonusMgtId", bonus.getId())
				.addParam("Locale", language)
				.addModel(bonus)
				.generate()
				.getFileLink();
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
	}

}
