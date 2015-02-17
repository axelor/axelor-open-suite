package com.axelor.apps.human.resource.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.human.resource.service.employee.EmployeeService;
import com.axelor.apps.human.resource.service.employee.EmployeeServiceImp;

@AxelorModuleInfo(name = "axelor-base")
public class HumanResourceModule extends AxelorModule {

	@Override
	protected void configure() {
		
		bind(EmployeeService.class).to(EmployeeServiceImp.class);
	}

}
