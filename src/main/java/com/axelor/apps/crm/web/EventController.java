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
