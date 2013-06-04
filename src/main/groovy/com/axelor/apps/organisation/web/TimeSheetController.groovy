package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.Move
import com.axelor.apps.base.db.Company
import com.axelor.apps.organisation.db.Timesheet
import com.axelor.apps.organisation.service.TimeSheetPeriodService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector;

@Slf4j
class TimeSheetController {

	@Inject
	private Injector injector


	def void getPeriod(ActionRequest request, ActionResponse response) {

		Timesheet timesheet = request.context as Timesheet
		Company company = timesheet.userInfo.activeCompany
		
		try {
			
			if(timesheet.fromDate && company)  {

				TimeSheetPeriodService ps = injector.getInstance(TimeSheetPeriodService.class)

				response.values = [
					"period" : ps.rightPeriod(timesheet.fromDate, company)
				]

			}
			else  {

				response.values = [
					"period" : null
				]

			}
		}
		catch (Exception e){ TraceBackService.trace(response, e) }
	}

}