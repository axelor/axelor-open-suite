/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
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
