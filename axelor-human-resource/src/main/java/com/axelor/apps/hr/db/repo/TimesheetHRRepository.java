package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;

public class TimesheetHRRepository extends TimesheetRepository{

	@Override
	public Timesheet save(Timesheet timesheet){
		
		for(TimesheetLine timesheetLine : timesheet.getTimesheetLineList()){
			timesheetLine.setFullName(timesheet.getFullName() + " " + timesheetLine.getDate() + " " + timesheetLine.getId());
		}
		
		return super.save(timesheet);
	}
	
}
