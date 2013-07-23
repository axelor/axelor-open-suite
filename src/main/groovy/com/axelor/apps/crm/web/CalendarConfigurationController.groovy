package com.axelor.apps.crm.web

import groovy.util.logging.Slf4j

import com.axelor.apps.crm.db.CalendarConfiguration
import com.axelor.apps.crm.service.CalendarConfigurationService;
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject;

@Slf4j
class CalendarConfigurationController {

	@Inject
	private CalendarConfigurationService calendarConfigurationService;
	
	def void createAction(ActionRequest request, ActionResponse response) {
		
		CalendarConfiguration calendarConfiguration = request.context as CalendarConfiguration
		
		calendarConfigurationService.createEntryMenu(calendarConfiguration)
		
	}
	
	def void deleteAction(ActionRequest request, ActionResponse response) {
	
		CalendarConfiguration calendarConfiguration = request.context as CalendarConfiguration
		
		calendarConfigurationService.deleteEntryMenu(calendarConfiguration);
	
	}
}
