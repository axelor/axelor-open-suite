package com.axelor.apps.crm.web

import groovy.util.logging.Slf4j
import org.joda.time.Duration

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Event
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.service.CalendarService
import com.axelor.apps.crm.service.EventService
import com.axelor.exception.AxelorException
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
public class CalendarController {

	@Inject
	private CalendarService calendarService
	
	def exportCalendar(ActionRequest request, ActionResponse response) {
		
		calendarService.exportCalendar()
	}
	
	def importCalendar(ActionRequest request, ActionResponse response) {
		
		calendarService.importCalendar()
	}
	
	def synchronizeCalendars(ActionRequest request, ActionResponse response) {
		
		calendarService.synchronizeCalendars(null)
	}
}
