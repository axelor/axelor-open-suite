package com.axelor.apps.hr.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.employee.EmployeeServiceImp;

@AxelorModuleInfo(name = "axelor-base")
public class HumanResourceModule extends AxelorModule {

	@Override
	protected void configure() {
		
		bind(EmployeeService.class).to(EmployeeServiceImp.class);
	}

}
