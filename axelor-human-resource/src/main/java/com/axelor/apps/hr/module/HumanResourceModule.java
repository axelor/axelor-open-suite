package com.axelor.apps.hr.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.base.service.batch.BatchReminderMail;
import com.axelor.apps.hr.service.batch.BatchReminderTimesheet;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.employee.EmployeeServiceImp;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.apps.hr.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.service.weeklyplanning.WeeklyPlanningServiceImp;

@AxelorModuleInfo(name = "axelor-human-resource")
public class HumanResourceModule extends AxelorModule {

	@Override
	protected void configure() {
		
		bind(EmployeeService.class).to(EmployeeServiceImp.class);
		bind(TimesheetService.class).to(TimesheetServiceImp.class);
		bind(WeeklyPlanningService.class).to(WeeklyPlanningServiceImp.class);
		bind(BatchReminderMail.class).to(BatchReminderTimesheet.class);
	}

}
