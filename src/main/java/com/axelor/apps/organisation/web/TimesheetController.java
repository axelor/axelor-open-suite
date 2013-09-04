package com.axelor.apps.organisation.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.service.TimesheetPeriodService;
import com.axelor.apps.organisation.service.TimesheetService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TimesheetController {

	@Inject
	private Provider<TimesheetPeriodService> timeSheetPeriodService;
	
	@Inject
	private Provider<TimesheetService> timesheetService;
	
	public void getPeriod(ActionRequest request, ActionResponse response) {

		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		Company company = timesheet.getUserInfo().getActiveCompany();
		
		try {
			
			if(timesheet.getFromDate() != null && company != null)  {

				response.setValue("period", timeSheetPeriodService.get().rightPeriod(timesheet.getFromDate(), company));
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void getTaskPastTime(ActionRequest request, ActionResponse response) {
		
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		
		timesheetService.get().getTaskPastTime(timesheet);
		
		response.setReload(true);
		
	}
}
