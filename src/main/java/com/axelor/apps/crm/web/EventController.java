package com.axelor.apps.crm.web;

import org.joda.time.Duration;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.service.EventService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventController {

	@Inject
	private EventService eventService;
	
	@Inject
	SequenceService sequenceService;
	
	public void computeStartDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		if(event.getStartDateTime() != null) {
			if(event.getEndDateTime() != null) {
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				response.setValue("durationHours", eventService.getHoursDuration(duration));
				response.setValue("durationMinutesSelect", eventService.getMinutesDuration(duration));
			}
			else if(event.getDurationHours() != null) {
				response.setValue("endDateTime", eventService.computeEndDateTime(event.getStartDateTime(), event.getDurationHours(), event.getDurationMinutesSelect()));
			}
		}
	}
	
	public void computeEndDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		if(event.getEndDateTime() != null) {
			if(event.getStartDateTime() != null) {
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				response.setValue("durationHours", eventService.getHoursDuration(duration));
				response.setValue("durationMinutesSelect", eventService.getMinutesDuration(duration));
			}
			else if(event.getDurationHours() != null)  {
				response.setValue("startDateTime", eventService.computeStartDateTime(event.getDurationHours(), event.getDurationMinutesSelect(), event.getEndDateTime()));
			}
		}
	}
	
	public void computeDuration(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		if(event.getDurationHours() != null)  {
			if(event.getStartDateTime() != null)  {
				response.setValue("endDateTime", eventService.computeEndDateTime(event.getStartDateTime(), event.getDurationHours(), event.getDurationMinutesSelect()));
			}
			else if(event.getEndDateTime() != null)  {
				response.setValue("startDateTime", eventService.computeStartDateTime(event.getDurationHours(), event.getDurationMinutesSelect(), event.getEndDateTime()));
			}
		}
	}
	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Event event = request.getContext().asType(Event.class);
		
		if(event.getTicketNumberSeq() ==  null && event.getTypeSelect() == IEvent.TICKET){
			String ref = sequenceService.getSequence(IAdministration.EVENT_TICKET,false);
			if (ref == null)
				throw new AxelorException("Aucune séquence configurée pour les tickets",
								IException.CONFIGURATION_ERROR);
			else
				response.setValue("ticketNumberSeq", ref);
		}
	}
}
