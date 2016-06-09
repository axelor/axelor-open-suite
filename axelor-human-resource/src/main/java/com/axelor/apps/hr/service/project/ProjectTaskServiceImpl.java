package com.axelor.apps.hr.service.project;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectTaskServiceImpl implements ProjectTaskService {
	
	@Inject
	protected GeneralService generalService;

	@Inject
	protected EmployeeService employeeService;	
	
	@Transactional(rollbackOn={Exception.class})
	public List<TimesheetLine> computeVisibleDuration(ProjectTask project){
		List<TimesheetLine> timesheetLineList = project.getTimesheetLineList();
		Employee timesheetEmployee;
		BigDecimal employeeDailyWorkHours;
		BigDecimal employeeDailyWorkHoursGeneral = generalService.getGeneral().getDailyWorkHours();
		
		for(TimesheetLine timesheetLine : timesheetLineList){
			timesheetEmployee = timesheetLine.getUser().getEmployee();
			if (timesheetEmployee == null || timesheetEmployee.getDailyWorkHours() == null)
				employeeDailyWorkHours = employeeDailyWorkHoursGeneral;
			else
				employeeDailyWorkHours = timesheetEmployee.getDailyWorkHours();
			timesheetLine.setVisibleDuration(employeeService.getUserDuration(timesheetLine.getDurationStored(), employeeDailyWorkHours, false));
		}
			

		timesheetLineList = _sortTimesheetLineByDate(timesheetLineList);

		return timesheetLineList;
	}

	public List<TimesheetLine> _sortTimesheetLineByDate(List<TimesheetLine> timesheetLineList){
	
		Collections.sort(timesheetLineList, new Comparator<TimesheetLine>() {
	
			@Override
			public int compare(TimesheetLine tsl1, TimesheetLine tsl2) {
				if(tsl1.getDate().isAfter(tsl2.getDate()))
					return 1;
				else if(tsl1.getDate().isBefore(tsl2.getDate()))
					return -1;
				else
					return 0;
			}
		});
	
		return timesheetLineList;
	}

}