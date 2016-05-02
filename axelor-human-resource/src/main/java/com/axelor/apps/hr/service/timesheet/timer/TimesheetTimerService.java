package com.axelor.apps.hr.service.timesheet.timer;

import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.TimesheetLine;

public interface TimesheetTimerService {

	public void pause(TSTimer timer);
	public void stop(TSTimer timer);
	public void calculateDuration(TSTimer timer);
	public TimesheetLine generateTimesheetLine(TSTimer timer);
	public TSTimer getCurrentTSTimer();
}
