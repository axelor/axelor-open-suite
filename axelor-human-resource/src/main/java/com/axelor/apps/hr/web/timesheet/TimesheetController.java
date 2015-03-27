package com.axelor.apps.hr.web.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TimesheetController {
	@Inject
	private TimesheetService timesheetService;
	
	public void getTimeFromTask(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheetService.getTimeFromTask(timesheet);
		response.setReload(true);
	}
	
	public void cancelTimesheet(ActionRequest request, ActionResponse response){
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheetService.cancelTimesheet(timesheet);
		response.setReload(true);
	}
	
	public void generateLines(ActionRequest request, ActionResponse response) throws AxelorException{
		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		timesheetService.generateLines(timesheet);
		response.setReload(true);
	}
	
}
