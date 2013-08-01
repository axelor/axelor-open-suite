package com.axelor.apps.crm.web;

import com.axelor.apps.crm.db.CalendarConfiguration;
import com.axelor.apps.crm.service.CalendarConfigurationService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class CalendarConfigurationController {

	@Inject
	private CalendarConfigurationService calendarConfigurationService;
	
	public void createAction(ActionRequest request, ActionResponse response) {
		
		CalendarConfiguration calendarConfiguration = request.getContext().asType(CalendarConfiguration.class);
		
		calendarConfigurationService.createEntryMenu(calendarConfiguration);		
	}
	
	public void deleteAction(ActionRequest request, ActionResponse response) {
	
		CalendarConfiguration calendarConfiguration = request.getContext().asType(CalendarConfiguration.class);
		
		calendarConfigurationService.deleteEntryMenu(calendarConfiguration);
	}
}
