package com.axelor.apps.hr.service.timesheet;

import org.joda.time.LocalDate;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public interface TimesheetService {
	public void getTimeFromTask(Timesheet timesheet);
	public void cancelTimesheet(Timesheet timesheet);
	public Timesheet generateLines(Timesheet timesheet) throws AxelorException;
	public LocalDate getFromPeriodDate();
	@Transactional
	public void computeTimeSpent(Timesheet timesheet);
	public void getActivities(ActionRequest request, ActionResponse response);
	@Transactional
	public void insertTSLine(ActionRequest request, ActionResponse response);
}
