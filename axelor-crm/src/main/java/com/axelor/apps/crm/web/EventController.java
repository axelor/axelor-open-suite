/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class EventController {

	private static final Logger LOG = LoggerFactory.getLogger(EventController.class);
	
	public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		EventService eventService = Beans.get(EventService.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getStartDateTime() != null) {
			if(event.getDuration() != null) {
				response.setValue("endDateTime", eventService.computeEndDateTime(event.getStartDateTime(), event.getDuration().intValue()));
			}
			else if(event.getEndDateTime() != null && event.getEndDateTime().isAfter(event.getStartDateTime())) {
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				response.setValue("duration", eventService.getDuration(duration));
			}
		}
	}
	
	public void computeFromEndDateTime(ActionRequest request, ActionResponse response) {
		
		Event event = request.getContext().asType(Event.class);
		EventService eventService = Beans.get(EventService.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getEndDateTime() != null) {
			if(event.getStartDateTime() != null && event.getStartDateTime().isBefore(event.getEndDateTime())) {
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
		EventService eventService = Beans.get(EventService.class);
		
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
		EventService eventService = Beans.get(EventService.class);
		
		LOG.debug("event : {}", event);
		
		if(event.getStartDateTime() != null && event.getEndDateTime() != null) {
			Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
			response.setValue("duration", eventService.getDuration(duration));
		}
	}
	
	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Event event = request.getContext().asType(Event.class);
		
		if(event.getTicketNumberSeq() ==  null && event.getTypeSelect() == IEvent.TICKET){
			String seq = Beans.get(SequenceService.class).getSequenceNumber(IAdministration.EVENT_TICKET);
			if (seq == null)
				throw new AxelorException(I18n.get(IExceptionMessage.EVENT_1),
								IException.CONFIGURATION_ERROR);
			else
				response.setValue("ticketNumberSeq", seq);
		}
	}
	
	//TODO : replace by XML action
	public void saveEventStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Event event = request.getContext().asType(Event.class);
		EventService eventService = Beans.get(EventService.class);
		Event persistEvent = eventService.find(event.getId());
		persistEvent.setStatusSelect(event.getStatusSelect());
		eventService.saveEvent(persistEvent);
		
	}
	
	//TODO : replace by XML action
	public void saveEventTaskStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Event event = request.getContext().asType(Event.class);
		EventService eventService = Beans.get(EventService.class);
		Event persistEvent = eventService.find(event.getId());
		persistEvent.setTaskStatusSelect(event.getTaskStatusSelect());
		eventService.saveEvent(persistEvent);
		
	}
	
	//TODO : replace by XML action
	public void saveEventTicketStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Event event = request.getContext().asType(Event.class);
		EventService eventService = Beans.get(EventService.class);
		Event persistEvent = eventService.find(event.getId());
		persistEvent.setTicketStatusSelect(event.getTicketStatusSelect());
		eventService.saveEvent(persistEvent);
		
	}
	
	public void viewMap(ActionRequest request, ActionResponse response)  {
		Event event = request.getContext().asType(Event.class);
		if(event.getLocation() != null){
			Map<String,Object> result = Beans.get(MapService.class).getMap(event.getLocation());
			if(result != null){
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", "Map");
				mapView.put("resource", result.get("url"));
				mapView.put("viewType", "html");
				response.setView(mapView);
			}
			else
				response.setFlash(String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ADDRESS_5),event.getLocation()));
		}else
			response.setFlash(I18n.get(IExceptionMessage.EVENT_2));
	}	
	
	
	public void addLeadAttendee(ActionRequest request, ActionResponse response)  {
		Lead lead = request.getContext().asType(Lead.class);
		
		if(lead != null)  {
			
			Event event = request.getContext().getParentContext().asType(Event.class);
			
			if(event != null)  {
				
				Beans.get(EventService.class).addLeadAttendee(event, lead, null);
				response.setReload(true);
				
			}
			
		}
		
	}
	
	public void assignToMeLead(ActionRequest request, ActionResponse response)  {
		LeadService leadService = Beans.get(LeadService.class);
		
		if(request.getContext().get("id") != null){
			Lead lead = leadService.find((Long)request.getContext().get("id"));
			lead.setUser(AuthUtils.getUser());
			if(lead.getStatusSelect() == 1)
				lead.setStatusSelect(2);
			leadService.saveLead(lead);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Lead lead : leadService.all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				lead.setUser(AuthUtils.getUser());
				if(lead.getStatusSelect() == 1)
					lead.setStatusSelect(2);
				leadService.saveLead(lead);
			}
		}
		response.setReload(true);
		
	}
	
	public void assignToMeEvent(ActionRequest request, ActionResponse response)  {
		
		EventService eventService = Beans.get(EventService.class);
		if(request.getContext().get("id") != null){
			Event event = eventService.find((Long)request.getContext().get("id"));
			event.setUser(AuthUtils.getUser());
			eventService.saveEvent(event);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Event event : eventService.all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				event.setUser(AuthUtils.getUser());
				eventService.saveEvent(event);
			}
		}
		response.setReload(true);
		
	}
	
	@Transactional
	public void reschedule(ActionRequest request, ActionResponse response)  {
		EventService eventService = Beans.get(EventService.class);
		
		Event eventPopup = request.getContext().asType(Event.class);
		Event event = eventService.find((Long)request.getContext().get("id"));
		event.setStartDateTime(eventPopup.getStartDateTime());
		if(event.getStartDateTime() != null) {
			if(event.getDuration() != null) {
				event.setEndDateTime(eventService.computeEndDateTime(event.getStartDateTime(), event.getDuration().intValue()));
			}
			else if(event.getEndDateTime() != null && event.getEndDateTime().isAfter(event.getStartDateTime())) {
				Duration duration =  eventService.computeDuration(event.getStartDateTime(), event.getEndDateTime());
				event.setDuration(new Long(eventService.getDuration(duration)));
			}
		}
		event.setStatusSelect(1);
		eventService.save(event);
		response.setCanClose(true);
	}
	
}
