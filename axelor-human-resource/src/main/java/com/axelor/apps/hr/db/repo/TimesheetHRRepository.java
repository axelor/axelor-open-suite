package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.inject.Beans;

public class TimesheetHRRepository extends TimesheetRepository{

	@Override
	public Timesheet save(Timesheet timesheet){
		if(timesheet.getTimesheetLineList() != null){
			for(TimesheetLine timesheetLine : timesheet.getTimesheetLineList())
				Beans.get(TimesheetLineHRRepository.class).computeFullName(timesheetLine);
		}
		return super.save(timesheet);
	}
	
}