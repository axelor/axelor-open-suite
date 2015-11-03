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

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import net.fortuna.ical4j.model.ValidationException;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.apps.crm.service.config.CrmConfigService;
import com.axelor.apps.message.db.Template;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class EventController {

	private static final Logger LOG = LoggerFactory.getLogger(EventController.class);
	
	@Inject
	private EventRepository eventRepo;
	
	@Inject
	private EventService eventService;
	
	@Inject
	private LeadRepository leadRepo;
	
	@Inject
	private LeadService leadService;

	public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {

		Event event = request.getContext().asType(Event.class);

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
			String seq = Beans.get(SequenceService.class).getSequenceNumber(IAdministration.EVENT_TICKET);
			if (seq == null)
				throw new AxelorException(I18n.get(IExceptionMessage.EVENT_1),
								IException.CONFIGURATION_ERROR);
			else
				response.setValue("ticketNumberSeq", seq);
		}
	}

	//TODO : replace by XML action
	public void saveEventTaskStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {

		Event event = request.getContext().asType(Event.class);
		Event persistEvent = eventRepo.find(event.getId());
		persistEvent.setTaskStatusSelect(event.getTaskStatusSelect());
		eventService.saveEvent(persistEvent);

	}

	//TODO : replace by XML action
	public void saveEventTicketStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {

		Event event = request.getContext().asType(Event.class);
		Event persistEvent = eventRepo.find(event.getId());
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

		if(request.getContext().get("id") != null){
			Lead lead = leadRepo.find((Long)request.getContext().get("id"));
			lead.setUser(AuthUtils.getUser());
			if(lead.getStatusSelect() == 1)
				lead.setStatusSelect(2);
			leadService.saveLead(lead);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Lead lead : leadRepo.all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				lead.setUser(AuthUtils.getUser());
				if(lead.getStatusSelect() == 1)
					lead.setStatusSelect(2);
				leadService.saveLead(lead);
			}
		}
		response.setReload(true);

	}

	public void assignToMeEvent(ActionRequest request, ActionResponse response)  {

		if(request.getContext().get("id") != null){
			Event event = eventRepo.find((Long)request.getContext().get("id"));
			event.setUser(AuthUtils.getUser());
			eventService.saveEvent(event);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Event event : eventRepo.all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				event.setUser(AuthUtils.getUser());
				eventService.saveEvent(event);
			}
		}
		response.setReload(true);

	}



	public void checkModifications(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, IOException, MessagingException  {

		Event event = request.getContext().asType(Event.class);
		Long idEvent = event.getId();
		Template deletedGuestsTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingGuestDeletedTemplate();
		Template addedGuestsTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingGuestAddedTemplate();
		Template changedDateTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingDateChangeTemplate();

		if(deletedGuestsTemplate == null && addedGuestsTemplate == null && changedDateTemplate == null){
			response.setFlash(String.format(I18n.get(IExceptionMessage.CRM_CONFIG_TEMPLATES_NONE),event.getUser().getActiveCompany().getName()));
		}
		else if(deletedGuestsTemplate == null || addedGuestsTemplate == null || changedDateTemplate == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CRM_CONFIG_TEMPLATES),event.getUser().getActiveCompany().getName()),
					IException.CONFIGURATION_ERROR);
		}
		else{
			if(idEvent != null && idEvent > 0){
				Event previousEvent = eventRepo.find(event.getId());
				event = eventService.checkModifications(event, previousEvent);
			}
			else{
				eventService.sendMails(event);
			}
		}
		
	}
	
	public void addUserGuest(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		Event event = request.getContext().asType(Event.class);
		if(request.getContext().containsKey("guestPartner")){
			User user = (User) request.getContext().get("guestUser");
			if(user != null){
				event = eventRepo.find(event.getId());
				eventService.addUserGuest(user, event);
			}
		}
		
		response.setReload(true);
	}
	
	public void addPartnerGuest(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		Event event = request.getContext().asType(Event.class);
		if(request.getContext().containsKey("guestPartner")){
			Partner partner = (Partner) request.getContext().get("guestPartner");
			if(partner != null){
				event = eventRepo.find(event.getId());
				eventService.addPartnerGuest(partner, event);
			}
		}
		
		response.setReload(true);
	}
	
	public void addEmailGuest(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		Event event = request.getContext().asType(Event.class);
		if(request.getContext().containsKey("guestEmail")){
			String email = request.getContext().get("guestEmail").toString();
			if(!Strings.isNullOrEmpty(email)){
				event = eventRepo.find(event.getId());
				eventService.addEmailGuest(email, event);
			}
		}
		response.setReload(true);
	}
	
}
