package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.exception.AxelorException;

public interface TimesheetService {
	public void getTimeFromTask(Timesheet timesheet);
	public void cancelTimesheet(Timesheet timesheet);
	public void generateLines(Timesheet timesheet) throws AxelorException;
}
