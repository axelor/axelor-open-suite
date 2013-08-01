package com.axelor.apps.organisation.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.service.TimeSheetPeriodService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class TimeSheetController {

	@Inject
	private Injector injector;
	
	public void getPeriod(ActionRequest request, ActionResponse response) {

		Timesheet timesheet = request.getContext().asType(Timesheet.class);
		Company company = timesheet.getUserInfo().getActiveCompany();
		
		try {
			
			if(timesheet.getFromDate() != null && company != null)  {

				TimeSheetPeriodService ps = injector.getInstance(TimeSheetPeriodService.class);
				
				response.setValue("period", ps.rightPeriod(timesheet.getFromDate(), company));
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
}
