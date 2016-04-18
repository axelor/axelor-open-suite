package com.axelor.apps.hr.service.timesheetTimer;

import com.axelor.apps.hr.db.TSTimer;

public interface TimesheetTimerService {

	public void pause(TSTimer timer);
	public void stop(TSTimer timer);
	public void calculateDuration(TSTimer timer);
	public void generateTimesheetLine(TSTimer timer);
	
}
