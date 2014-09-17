/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.axelor.auth.AuthUtils;

public class EventController {

	private static final Logger LOG = LoggerFactory.getLogger(EventController.class);
	
	@Inject
	private Provider<EventService> eventProvider;
	
	@Inject
	private Provider<SequenceService> sequenceProvider;
	
	@Inject
	private Provider<MapService> mapProvider;
	
	@Inject
	private Provider<LeadService> leadProvider;
	
	
	public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getStartDateTime() != null) {
			if(event.getEndDateTime() != null) {
				EventService eventService = eventProvider.get();
				
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				response.setValue("duration", eventService.getDuration(duration));
			}
			else if(event.getDuration() != null) {
				response.setValue("endDateTime", eventProvider.get().computeEndDateTime(event.getStartDateTime(), event.getDuration().intValue()));
			}
		}
	}
	
	public void computeFromEndDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getEndDateTime() != null) {
			if(event.getStartDateTime() != null) {
				EventService eventService = eventProvider.get();
				
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				response.setValue("duration", eventService.getDuration(duration));
			}
			else if(event.getDuration() != null)  {
				response.setValue("startDateTime", eventProvider.get().computeStartDateTime(event.getDuration().intValue(), event.getEndDateTime()));
			}
		}
	}
	
	public void computeFromDuration(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getDuration() != null)  {
			if(event.getStartDateTime() != null)  {
				response.setValue("endDateTime", eventProvider.get().computeEndDateTime(event.getStartDateTime(), event.getDuration().intValue()));
			}
			else if(event.getEndDateTime() != null)  {
				response.setValue("startDateTime", eventProvider.get().computeStartDateTime(event.getDuration().intValue(), event.getEndDateTime()));
			}
		}
	}
	
	
	public void computeFromCalendar(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getStartDateTime() != null && event.getEndDateTime() != null) {
			EventService eventService = eventProvider.get();
			
			Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
			response.setValue("duration", eventService.getDuration(duration));
		}
	}
	
	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Event event = request.getContext().asType(Event.class);
		
		if(event.getTicketNumberSeq() ==  null && event.getTypeSelect() == IEvent.TICKET){
			String seq = sequenceProvider.get().getSequenceNumber(IAdministration.EVENT_TICKET);
			if (seq == null)
				throw new AxelorException("Aucune séquence configurée pour les tickets",
								IException.CONFIGURATION_ERROR);
			else
				response.setValue("ticketNumberSeq", seq);
		}
	}
	
	//TODO : replace by XML action
	public void saveEventStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		Event event = request.getContext().asType(Event.class);
		Event persistEvent = eventProvider.get().find(event.getId());
		persistEvent.setStatusSelect(event.getStatusSelect());
		eventProvider.get().saveEvent(persistEvent);
	}
	
	//TODO : replace by XML action
	public void saveEventTaskStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		Event event = request.getContext().asType(Event.class);
		Event persistEvent = eventProvider.get().find(event.getId());
		persistEvent.setTaskStatusSelect(event.getTaskStatusSelect());
		eventProvider.get().saveEvent(persistEvent);
	}
	
	//TODO : replace by XML action
	public void saveEventTicketStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		Event event = request.getContext().asType(Event.class);
		Event persistEvent = eventProvider.get().find(event.getId());
		persistEvent.setTicketStatusSelect(event.getTicketStatusSelect());
		eventProvider.get().saveEvent(persistEvent);
	}
	
	public void viewMap(ActionRequest request, ActionResponse response)  {
		Event event = request.getContext().asType(Event.class);
		if(event.getLocation() != null){
			Map<String,Object> result = mapProvider.get().getMap(event.getLocation(), BigDecimal.ZERO, BigDecimal.ZERO);
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
				
				eventProvider.get().addLeadAttendee(event, lead, null);
				response.setReload(true);
				
			}
			
		}
		
	}
	
	public void assignToMeLead(ActionRequest request, ActionResponse response)  {
		if(request.getContext().get("id") != null){
			Lead lead = leadProvider.get().find((Long)request.getContext().get("id"));
			lead.setUser(AuthUtils.getUser());
			if(lead.getStatusSelect() == 1)
				lead.setStatusSelect(2);
			leadProvider.get().saveLead(lead);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Lead lead : leadProvider.get().all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				lead.setUser(AuthUtils.getUser());
				if(lead.getStatusSelect() == 1)
					lead.setStatusSelect(2);
				leadProvider.get().saveLead(lead);
			}
		}
		response.setReload(true);
		
	}
	
	public void assignToMeEvent(ActionRequest request, ActionResponse response)  {
		if(request.getContext().get("id") != null){
			Event event = eventProvider.get().find((Long)request.getContext().get("id"));
			event.setUser(AuthUtils.getUser());
			eventProvider.get().saveEvent(event);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Event event : eventProvider.get().all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				event.setUser(AuthUtils.getUser());
				eventProvider.get().saveEvent(event);
			}
		}
		response.setReload(true);
		
	}
	
	
}
