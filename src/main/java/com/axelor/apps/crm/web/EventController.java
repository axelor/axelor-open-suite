/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.crm.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.service.EventService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventController {

	private static final Logger LOG = LoggerFactory.getLogger(EventController.class);
	
	@Inject
	private EventService eventService;
	
	@Inject
	SequenceService sequenceService;
	
	@Inject
	AddressService ads;
	
	public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getStartDateTime() != null) {
			if(event.getEndDateTime() != null) {
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				response.setValue("duration", eventService.getDuration(duration));
			}
			else if(event.getDuration() != null) {
				response.setValue("endDateTime", eventService.computeEndDateTime(event.getStartDateTime(), event.getDuration().intValue()));
			}
		}
	}
	
	public void computeFromEndDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getEndDateTime() != null) {
			if(event.getStartDateTime() != null) {
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				response.setValue("duration", eventService.getDuration(duration));
			}
			else if(event.getDuration() != null)  {
				response.setValue("startDateTime", eventService.computeStartDateTime(event.getDuration().intValue(), event.getEndDateTime()));
			}
		}
	}
	
	public void computeFromDuration(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getDuration() != null)  {
			if(event.getStartDateTime() != null)  {
				response.setValue("endDateTime", eventService.computeEndDateTime(event.getStartDateTime(), event.getDuration().intValue()));
			}
			else if(event.getEndDateTime() != null)  {
				response.setValue("startDateTime", eventService.computeStartDateTime(event.getDuration().intValue(), event.getEndDateTime()));
			}
		}
	}
	
	
	public void computeFromCalendar(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getStartDateTime() != null && event.getEndDateTime() != null) {
			Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
			response.setValue("duration", eventService.getDuration(duration));
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
	
	public void saveEventStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		Event event = request.getContext().asType(Event.class);
		Event persistEvent = Event.find(event.getId());
		persistEvent.setStatusSelect(event.getStatusSelect());
		eventService.saveEvent(persistEvent);
	}
	
	public void saveEventTaskStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		Event event = request.getContext().asType(Event.class);
		Event persistEvent = Event.find(event.getId());
		persistEvent.setTaskStatusSelect(event.getTaskStatusSelect());
		eventService.saveEvent(persistEvent);
	}
	
	public void saveEventTicketStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		Event event = request.getContext().asType(Event.class);
		Event persistEvent = Event.find(event.getId());
		persistEvent.setTicketStatusSelect(event.getTicketStatusSelect());
		eventService.saveEvent(persistEvent);
	}
	
	public void viewMap(ActionRequest request, ActionResponse response)  {
		Event event = request.getContext().asType(Event.class);
		if(event.getLocation() != null){
			Map<String,Object> result = ads.getMap(event.getLocation(), BigDecimal.ZERO, BigDecimal.ZERO);
			if(result != null){
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", "Map");
				mapView.put("resource", result.get("url"));
				mapView.put("viewType", "html");
				response.setView(mapView);
			}
			else
				response.setFlash(String.format("<B>%s</B> not found",event.getLocation()));
		}else
			response.setFlash("Input location please !");
	}	
	
	
	public void addLeadAttendee(ActionRequest request, ActionResponse response)  {
		Lead lead = request.getContext().asType(Lead.class);
		
		if(lead != null)  {
			
			Event event = request.getContext().getParentContext().asType(Event.class);
			
			if(event != null)  {
				
				eventService.addLeadAttendee(event, lead, null);
				response.setReload(true);
				
			}
			
		}
		
	}
}
