package com.axelor.csv.script;

import java.math.BigDecimal;

import com.axelor.apps.hr.service.employee.EmployeeService;
import com.google.inject.Inject;

public class ImportDurationStored {
	
	@Inject
	protected EmployeeService employeeService;
	
	public String getDurationHoursImport(String duration){
		BigDecimal visibleDuration = new BigDecimal(duration);
		BigDecimal durationStored = employeeService.getDurationHours(visibleDuration);
		return durationStored.toString();
	}
}
