package com.axelor.apps.crm.web

import groovy.util.logging.Slf4j
import org.joda.time.Duration

import com.axelor.apps.base.db.Partner
import com.axelor.apps.crm.db.Event
import com.axelor.apps.crm.service.EventService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
public class EventController {

	@Inject
	private EventService eventService
	
	def computeStartDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.context as Event
		
		if(event.startDateTime != null)  {
			if(event.endDateTime != null)  {
				Duration duration =  eventService.computeDuration(event.startDateTime, event.endDateTime)
				response.values = [ "durationHours" : eventService.getHoursDuration(duration),
									"durationMinutesSelect" : eventService.getMinutesDuration(duration)]
			}
			else if(event.durationHours != null)  {
				response.values = [ "endDateTime" : eventService.computeEndDateTime(event.startDateTime, event.durationHours, event.durationMinutesSelect)]
			}
		}
	}
	
	def computeEndDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.context as Event
		
		if(event.endDateTime != null)  {
			if(event.startDateTime != null)  {
				Duration duration =  eventService.computeDuration(event.startDateTime, event.endDateTime)
				response.values = [ "durationHours" : eventService.getHoursDuration(duration),
									"durationMinutesSelect" : eventService.getMinutesDuration(duration)]
			}
			else if(event.durationHours != null)  {
				response.values = [ "startDateTime" : eventService.computeEndDateTime(event.durationHours, event.durationMinutesSelect, event.endDateTime)]
			}
		}
	}
	
	def computeDuration(ActionRequest request, ActionResponse response) {
		
		Event event = request.context as Event
		
		if(event.durationHours != null)  {
			if(event.startDateTime != null)  {
				response.values = [ "endDateTime" : eventService.computeEndDateTime(event.startDateTime, event.durationHours, event.durationMinutesSelect)]
			}
			else if(event.endDateTime != null)  {
				response.values = [ "startDateTime" : eventService.computeEndDateTime(event.durationHours, event.durationMinutesSelect, event.endDateTime)]
			}
		}
	}
  
}
