/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.db.ILead;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.RecurrenceConfiguration;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.RecurrenceConfigurationRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.apps.crm.service.config.CrmConfigService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import net.fortuna.ical4j.model.ValidationException;

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
	
	@Inject
	protected CalendarService calendarService;

	public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {

		Event event = request.getContext().asType(Event.class);

		LOG.debug("event : {}", event);

		if(event.getStartDateTime() != null) {
			if(event.getDuration() != null && event.getDuration() != 0) {
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
		persistEvent.setStatusSelect(event.getStatusSelect());
		eventService.saveEvent(persistEvent);

	}

	//TODO : replace by XML action
	public void saveEventTicketStatusSelect(ActionRequest request, ActionResponse response) throws AxelorException {

		Event event = request.getContext().asType(Event.class);
		Event persistEvent = eventRepo.find(event.getId());
		persistEvent.setStatusSelect(event.getStatusSelect());
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

	@SuppressWarnings("rawtypes")
	public void assignToMeLead(ActionRequest request, ActionResponse response)  {

		if(request.getContext().get("id") != null){
			Lead lead = leadRepo.find((Long)request.getContext().get("id"));
			lead.setUser(AuthUtils.getUser());
			if(lead.getStatusSelect() == ILead.STATUS_NEW)
				lead.setStatusSelect(ILead.STATUS_ASSIGNED);
			leadService.saveLead(lead);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Lead lead : leadRepo.all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				lead.setUser(AuthUtils.getUser());
				if(lead.getStatusSelect() == ILead.STATUS_NEW)
					lead.setStatusSelect(ILead.STATUS_ASSIGNED);
				leadService.saveLead(lead);
			}
		}
		response.setReload(true);

	}

	@SuppressWarnings("rawtypes")
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
	
	@SuppressWarnings("unchecked")
	public void addUserGuest(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		Event event = request.getContext().asType(Event.class);
		if(request.getContext().containsKey("guestUser")){
			User user = Beans.get(UserRepository.class).find(new Long(((Map<String, Object>) request.getContext().get("guestUser")).get("id").toString()));
			if(user != null){
				event = eventRepo.find(event.getId());
				eventService.addUserGuest(user, event);
			}
		}
		
		response.setReload(true);
	}
	
	@SuppressWarnings("unchecked")
	public void addPartnerGuest(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		Event event = request.getContext().asType(Event.class);
		if(request.getContext().containsKey("guestPartner")){
			Partner partner = Beans.get(PartnerRepository.class).find(new Long(((Map<String, Object>) request.getContext().get("guestPartner")).get("id").toString()));
			if(partner != null){
				event = eventRepo.find(event.getId());
				eventService.addPartnerGuest(partner, event);
			}
		}
		
		response.setReload(true);
	}
	
	@SuppressWarnings("unchecked")
	public void addEmailGuest(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		Event event = request.getContext().asType(Event.class);
		if(request.getContext().containsKey("guestEmail")){
			EmailAddress emailAddress = Beans.get(EmailAddressRepository.class).find(new Long(((Map<String, Object>) request.getContext().get("guestEmail")).get("id").toString()));
			if(emailAddress != null){
				event = eventRepo.find(event.getId());
				eventService.addEmailGuest(emailAddress, event);
			}
		}
		response.setReload(true);
	}
	
	@Transactional
	public void generateRecurrentEvents(ActionRequest request, ActionResponse response) throws AxelorException{
		Long eventId = new Long(request.getContext().get("_idEvent").toString());
		Event event = eventRepo.find(eventId);
		RecurrenceConfiguration conf = request.getContext().asType(RecurrenceConfiguration.class);
		RecurrenceConfigurationRepository confRepo = Beans.get(RecurrenceConfigurationRepository.class);
		conf = confRepo.save(conf);
		event.setRecurrenceConfiguration(conf);
		event = eventRepo.save(event);
		if(request.getContext().get("recurrenceType") == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_RECURRENCE_TYPE)),
					IException.CONFIGURATION_ERROR);
		}
		
		int recurrenceType = new Integer(request.getContext().get("recurrenceType").toString());
		
		if(request.getContext().get("periodicity") == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_PERIODICITY)),
					IException.CONFIGURATION_ERROR);
		}
		
		int periodicity = new Integer(request.getContext().get("periodicity").toString());
		
		if(periodicity < 1){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_PERIODICITY)),
					IException.CONFIGURATION_ERROR);
		}
		
		boolean monday = (boolean)request.getContext().get("monday");
		boolean tuesday = (boolean)request.getContext().get("tuesday");
		boolean wednesday = (boolean)request.getContext().get("wednesday");
		boolean thursday = (boolean)request.getContext().get("thursday");
		boolean friday = (boolean)request.getContext().get("friday");
		boolean saturday = (boolean)request.getContext().get("saturday");
		boolean sunday = (boolean)request.getContext().get("sunday");
		Map<Integer,Boolean> daysMap = new HashMap<Integer,Boolean>();
		Map<Integer,Boolean> daysCheckedMap = new HashMap<Integer,Boolean>();
		if(recurrenceType == 2){
			daysMap.put(DateTimeConstants.MONDAY, monday);
			daysMap.put(DateTimeConstants.TUESDAY, tuesday);
			daysMap.put(DateTimeConstants.WEDNESDAY, wednesday);
			daysMap.put(DateTimeConstants.THURSDAY, thursday);
			daysMap.put(DateTimeConstants.FRIDAY, friday);
			daysMap.put(DateTimeConstants.SATURDAY, saturday);
			daysMap.put(DateTimeConstants.SUNDAY, sunday);
			
			for (Integer day : daysMap.keySet()) {
				if(daysMap.get(day)){
					daysCheckedMap.put(day, daysMap.get(day));
				}
			}
			if(daysMap.isEmpty()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_DAYS_CHECKED)),
						IException.CONFIGURATION_ERROR);
			}
		}
		
		int monthRepeatType = new Integer(request.getContext().get("monthRepeatType").toString());
		
		int endType = new Integer(request.getContext().get("endType").toString());
		
		int repetitionsNumber = 0;
		
		if(endType == 1 ){
			if(request.getContext().get("repetitionsNumber") == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_REPETITION_NUMBER)),
						IException.CONFIGURATION_ERROR);
			}
			
			repetitionsNumber = new Integer(request.getContext().get("repetitionsNumber").toString());
			
			if(repetitionsNumber < 1){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_REPETITION_NUMBER)),
						IException.CONFIGURATION_ERROR);
			}
		}
		LocalDate endDate = new LocalDate();
		if(endType == 2){
			if(request.getContext().get("endDate") == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_END_DATE)),
						IException.CONFIGURATION_ERROR);
			}
			
			endDate = new LocalDate(request.getContext().get("endDate").toString());
			
			if(endDate.isBefore(event.getStartDateTime()) && endDate.isEqual(event.getStartDateTime())){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_END_DATE)),
						IException.CONFIGURATION_ERROR);
			}
		}
		switch (recurrenceType) {
		case 1:
			eventService.addRecurrentEventsByDays(event, periodicity, endType, repetitionsNumber, endDate);
			break;
		
		case 2:
			eventService.addRecurrentEventsByWeeks(event, periodicity, endType, repetitionsNumber, endDate, daysCheckedMap);
			break;
		
		case 3:
			eventService.addRecurrentEventsByMonths(event, periodicity, endType, repetitionsNumber, endDate, monthRepeatType);
			break;
		
		case 4:
			eventService.addRecurrentEventsByYears(event, periodicity, endType, repetitionsNumber, endDate);
			break;

		default:
			break;
		}
		
		response.setCanClose(true);
		response.setReload(true);
	}
	@Transactional
	public void deleteThis(ActionRequest request, ActionResponse response) throws AxelorException{
		Long eventId = new Long(request.getContext().get("_idEvent").toString());
		Event event = eventRepo.find(eventId);
		Event child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		if(child != null){
			child.setParentEvent(event.getParentEvent());
		}
		eventRepo.remove(event);
		response.setCanClose(true);
		response.setReload(true);
	}
	@Transactional
	public void deleteNext(ActionRequest request, ActionResponse response) throws AxelorException{
		Long eventId = new Long(request.getContext().get("_idEvent").toString());
		Event event = eventRepo.find(eventId);
		Event child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		while(child != null){
			child.setParentEvent(null);
			eventRepo.remove(event);
			event = child;
			child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		}
		response.setCanClose(true);
		response.setReload(true);
	}
	@Transactional
	public void deleteAll(ActionRequest request, ActionResponse response) throws AxelorException{
		Long eventId = new Long(request.getContext().get("_idEvent").toString());
		Event event = eventRepo.find(eventId);
		Event child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		Event parent = event.getParentEvent();
		while(child != null){
			child.setParentEvent(null);
			eventRepo.remove(event);
			event = child;
			child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		}
		while(parent != null){
			Event nextParent = parent.getParentEvent();
			eventRepo.remove(parent);
			parent = nextParent;
		}
		response.setCanClose(true);
		response.setReload(true);
	}
	
	@Transactional
	public void changeAll(ActionRequest request, ActionResponse response) throws AxelorException{
		Long eventId = new Long(request.getContext().get("_idEvent").toString());
		Event event = eventRepo.find(eventId);
		
		Event child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		Event parent = event.getParentEvent();
		child.setParentEvent(null);
		Event eventDeleted = child;
		child = eventRepo.all().filter("self.parentEvent.id = ?1", eventDeleted.getId()).fetchOne();
		while(child != null){
			child.setParentEvent(null);
			eventRepo.remove(eventDeleted);
			eventDeleted = child;
			child = eventRepo.all().filter("self.parentEvent.id = ?1", eventDeleted.getId()).fetchOne();
		}
		while(parent != null){
			Event nextParent = parent.getParentEvent();
			eventRepo.remove(parent);
			parent = nextParent;
		}
		
		
		RecurrenceConfiguration conf = request.getContext().asType(RecurrenceConfiguration.class);
		RecurrenceConfigurationRepository confRepo = Beans.get(RecurrenceConfigurationRepository.class);
		conf = confRepo.save(conf);
		event.setRecurrenceConfiguration(conf);
		event = eventRepo.save(event);
		if(conf.getRecurrenceType() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_RECURRENCE_TYPE)),
					IException.CONFIGURATION_ERROR);
		}
		
		int recurrenceType = conf.getRecurrenceType();
		
		if(conf.getPeriodicity() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_PERIODICITY)),
					IException.CONFIGURATION_ERROR);
		}
		
		int periodicity = conf.getPeriodicity();
		
		if(periodicity < 1){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_PERIODICITY)),
					IException.CONFIGURATION_ERROR);
		}
		
		boolean monday = conf.getMonday();
		boolean tuesday = conf.getTuesday();
		boolean wednesday = conf.getWednesday();
		boolean thursday = conf.getThursday();
		boolean friday = conf.getFriday();
		boolean saturday = conf.getSaturday();
		boolean sunday = conf.getSunday();
		Map<Integer,Boolean> daysMap = new HashMap<Integer,Boolean>();
		Map<Integer,Boolean> daysCheckedMap = new HashMap<Integer,Boolean>();
		if(recurrenceType == 2){
			daysMap.put(DateTimeConstants.MONDAY, monday);
			daysMap.put(DateTimeConstants.TUESDAY, tuesday);
			daysMap.put(DateTimeConstants.WEDNESDAY, wednesday);
			daysMap.put(DateTimeConstants.THURSDAY, thursday);
			daysMap.put(DateTimeConstants.FRIDAY, friday);
			daysMap.put(DateTimeConstants.SATURDAY, saturday);
			daysMap.put(DateTimeConstants.SUNDAY, sunday);
			
			for (Integer day : daysMap.keySet()) {
				if(daysMap.get(day)){
					daysCheckedMap.put(day, daysMap.get(day));
				}
			}
			if(daysMap.isEmpty()){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_DAYS_CHECKED)),
						IException.CONFIGURATION_ERROR);
			}
		}
		
		int monthRepeatType = conf.getMonthRepeatType();
		
		int endType = conf.getEndType();
		
		int repetitionsNumber = 0;
		
		if(endType == 1 ){
			if(conf.getRepetitionsNumber() == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_REPETITION_NUMBER)),
						IException.CONFIGURATION_ERROR);
			}
			
			repetitionsNumber = conf.getRepetitionsNumber();
			
			if(repetitionsNumber < 1){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_REPETITION_NUMBER)),
						IException.CONFIGURATION_ERROR);
			}
		}
		LocalDate endDate = new LocalDate();
		if(endType == 2){
			if(conf.getEndDate() == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_END_DATE)),
						IException.CONFIGURATION_ERROR);
			}
			
			endDate = new LocalDate(conf.getEndDate());
			
			if(endDate.isBefore(event.getStartDateTime()) && endDate.isEqual(event.getStartDateTime())){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECURRENCE_END_DATE)),
						IException.CONFIGURATION_ERROR);
			}
		}
		switch (recurrenceType) {
		case 1:
			eventService.addRecurrentEventsByDays(event, periodicity, endType, repetitionsNumber, endDate);
			break;
		
		case 2:
			eventService.addRecurrentEventsByWeeks(event, periodicity, endType, repetitionsNumber, endDate, daysCheckedMap);
			break;
		
		case 3:
			eventService.addRecurrentEventsByMonths(event, periodicity, endType, repetitionsNumber, endDate, monthRepeatType);
			break;
		
		case 4:
			eventService.addRecurrentEventsByYears(event, periodicity, endType, repetitionsNumber, endDate);
			break;

		default:
			break;
		}
		
		response.setCanClose(true);
		response.setReload(true);
	}
	

	public void applyChangesToAll(ActionRequest request, ActionResponse response){
		Event thisEvent = eventRepo.find(new Long(request.getContext().get("_idEvent").toString()));
		Event event = eventRepo.find(thisEvent.getId());
		
		eventService.applyChangesToAll(event);
		response.setCanClose(true);
		response.setReload(true);
	}
	
	public void computeRecurrenceName(ActionRequest request, ActionResponse response){
		RecurrenceConfiguration recurrConf = request.getContext().asType(RecurrenceConfiguration.class);
		
		response.setValue("recurrenceName", eventService.computeRecurrenceName(recurrConf));
	}
	
	public void setCalendarCrmDomain(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		List<Long> calendarIdlist = calendarService.showSharedCalendars(user);
		if(calendarIdlist.isEmpty()){
			response.setAttr("calendarCrm", "domain", "self.id is null");
		}
		else{
			response.setAttr("calendarCrm", "domain", "self.id in (" + Joiner.on(",").join(calendarIdlist) + ")");
		}
	}
	
	public void checkRights(ActionRequest request, ActionResponse response){
		Event event = request.getContext().asType(Event.class);
		User user = AuthUtils.getUser();
		List<Long> calendarIdlist = calendarService.showSharedCalendars(user);
		if(calendarIdlist.isEmpty() || !calendarIdlist.contains(event.getCalendarCrm().getId())){
			response.setAttr("calendarConfig", "readonly", "true");
			response.setAttr("meetingGeneral", "readonly", "true");
			response.setAttr("addGuests", "readonly", "true");
			response.setAttr("meetingAttributes", "readonly", "true");
			response.setAttr("meetingLinked", "readonly", "true");
		}
	}
	
	public void changeCreator(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		response.setValue("organizer", calendarService.findOrCreateUser(user));
	}
}
