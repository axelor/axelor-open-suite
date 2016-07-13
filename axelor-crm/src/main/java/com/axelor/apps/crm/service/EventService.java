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
package com.axelor.apps.crm.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.RecurrenceConfiguration;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.RecurrenceConfigurationRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.config.CrmConfigService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailAddress;
import com.axelor.mail.db.repo.MailAddressRepository;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.Method;

public class EventService {

	@Inject
	private EventAttendeeService eventAttendeeService;
	
	@Inject
	private PartnerService partnerService;
	
	@Inject
	private EventRepository eventRepo;
	
	@Inject
	protected MailFollowerRepository mailFollowerRepo;
	
	static final String REQUEST = "REQUEST";

	public Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime)  {

		return new Interval(startDateTime.toDateTime(), endDateTime.toDateTime()).toDuration();

	}

	public int getDuration(Duration duration)  {

		return duration.toStandardSeconds().getSeconds();

	}

	public LocalDateTime computeStartDateTime(int duration, LocalDateTime endDateTime)  {

		return endDateTime.minusSeconds(duration);

	}

	public LocalDateTime computeEndDateTime(LocalDateTime startDateTime, int duration)  {

		return startDateTime.plusSeconds(duration);

	}

	@Transactional
	public void saveEvent(Event event){
		eventRepo.save(event);
	}


	@Transactional
	public void addLeadAttendee(Event event, Lead lead, Partner contactPartner)  {

		event.addEventAttendeeListItem(eventAttendeeService.createEventAttendee(event, lead, contactPartner));
		eventRepo.save(event);

	}

	public Event createEvent(LocalDateTime fromDateTime, LocalDateTime toDateTime, User user, String description, int type, String subject){
		Event event = new Event();
		event.setSubject(subject);
		event.setStartDateTime(fromDateTime);
		event.setEndDateTime(toDateTime);
		event.setUser(user);
		event.setTypeSelect(type);
		if(!Strings.isNullOrEmpty(description)){
			event.setDescription(description);
		}
		return event;
	}
	
	public String getInvoicingAddressFullName(Partner partner) {

		Address address = partnerService.getInvoicingAddress(partner);
		if(address != null){
			return address.getFullName();
		}
		
		return null;
	}
	
	public void manageFollowers(Event event){
		List<ICalendarUser> attendeesSet = event.getAttendees();
		if(attendeesSet != null){
			for (ICalendarUser user : attendeesSet) {
				if(user.getUser() != null){
					mailFollowerRepo.follow(event, user.getUser());
				}
				else{
					MailAddress mailAddress = Beans.get(MailAddressRepository.class).findOrCreate(user.getEmail(), user.getName());
					mailFollowerRepo.follow(event, mailAddress);
				}
				
			}
		}
	}
	
	public Event checkModifications(Event event, Event previousEvent) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, MessagingException{
		Set<User> previousUserSet = previousEvent.getInternalGuestSet();
		Set<User> userSet = event.getInternalGuestSet();
		Set<Partner> previousContactSet = previousEvent.getExternalGuestSet();
		Set<Partner> contactSet = previousEvent.getExternalGuestSet();

		List<User> deletedUsers = this.deletedGuests(previousUserSet, userSet);
		List<User> addedUsers = this.addedGuests(previousUserSet, userSet);

		List<Partner> deletedContacts = this.deletedGuests(previousContactSet, contactSet);
		List<Partner> addedContacts = this.addedGuests(previousContactSet, contactSet);

		Template deletedGuestsTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingGuestDeletedTemplate();
		Template addedGuestsTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingGuestAddedTemplate();
		Template changedDateTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingDateChangeTemplate();

		if(deletedGuestsTemplate == null || addedGuestsTemplate == null || changedDateTemplate == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CRM_CONFIG_TEMPLATES),event.getUser().getActiveCompany()),
					IException.CONFIGURATION_ERROR);
		}
		if(!event.getEndDateTime().isEqual(previousEvent.getEndDateTime())){
			for (Partner partner : contactSet) {
				Message message = Beans.get(TemplateMessageService.class).generateMessage(event, changedDateTemplate);
				if(message.getFromEmailAddress() == null){
					message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
				}
				message.addToEmailAddressSetItem(partner.getEmailAddress());
				message = Beans.get(MessageService.class).sendByEmail(message);
			}
			for (User user : userSet) {
				Message message = Beans.get(TemplateMessageService.class).generateMessage(event, changedDateTemplate);
				if(message.getFromEmailAddress() == null){
					message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
				}
				message.addToEmailAddressSetItem(user.getPartner().getEmailAddress());
				message = Beans.get(MessageService.class).sendByEmail(message);
			}
			Message message = Beans.get(TemplateMessageService.class).generateMessage(event, changedDateTemplate);
			if(message.getFromEmailAddress() == null){
				message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
			}
			message.addToEmailAddressSetItem(event.getUser().getPartner().getEmailAddress());
			message = Beans.get(MessageService.class).sendByEmail(message);

		}
		for (Partner partner : addedContacts) {
			Message message = Beans.get(TemplateMessageService.class).generateMessage(event, addedGuestsTemplate);
			if(message.getFromEmailAddress() == null){
				message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
			}
			message.addToEmailAddressSetItem(partner.getEmailAddress());
			message = Beans.get(MessageService.class).sendByEmail(message);
		}
		for (Partner partner : deletedContacts) {
			Message message = Beans.get(TemplateMessageService.class).generateMessage(event, deletedGuestsTemplate);
			if(message.getFromEmailAddress() == null){
				message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
			}
			message.addToEmailAddressSetItem(partner.getEmailAddress());
			message = Beans.get(MessageService.class).sendByEmail(message);
		}
		for (User user : addedUsers) {
			Message message = Beans.get(TemplateMessageService.class).generateMessage(event, addedGuestsTemplate);
			if(message.getFromEmailAddress() == null){
				message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
			}
			message.addToEmailAddressSetItem(user.getPartner().getEmailAddress());
			message = Beans.get(MessageService.class).sendByEmail(message);
		}
		for (User user : deletedUsers) {
			Message message = Beans.get(TemplateMessageService.class).generateMessage(event, deletedGuestsTemplate);
			if(message.getFromEmailAddress() == null){
				message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
			}
			message.addToEmailAddressSetItem(user.getPartner().getEmailAddress());
			message = Beans.get(MessageService.class).sendByEmail(message);
		}
		return event;
	}

	public <T> List<T> deletedGuests (Set<T> previousSet, Set<T> set){
		List<T> deletedList = new ArrayList<T>();
		if(previousSet != null){
			for (T object : previousSet) {
				if(set == null || set.isEmpty() || !set.contains(object)){
					deletedList.add(object);
				}
			}
		}
		return deletedList;
	}

	public <T> List<T> addedGuests (Set<T> previousSet, Set<T> set){
		List<T> addedList = new ArrayList<T>();
		if(set != null){
			for (T object : set) {
				if(previousSet == null || previousSet.isEmpty() || !previousSet.contains(object)){
					addedList.add(object);
				}
			}
		}
		return addedList;
	}

	public void sendMails(Event event) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, MessagingException{
		Template guestAddedTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingGuestAddedTemplate();
		if(guestAddedTemplate == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CRM_CONFIG_TEMPLATES),event.getUser().getActiveCompany()),
					IException.CONFIGURATION_ERROR);
		}
		if(event.getExternalGuestSet() != null){
			for (Partner partner : event.getExternalGuestSet()) {
				Message message = Beans.get(TemplateMessageService.class).generateMessage(event, guestAddedTemplate);
				if(message.getFromEmailAddress() == null){
					message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
				}
				message.addToEmailAddressSetItem(partner.getEmailAddress());
				message = Beans.get(MessageService.class).sendByEmail(message);
			}
		}
		if(event.getInternalGuestSet() != null){
			for (User user : event.getInternalGuestSet()) {
				Message message = Beans.get(TemplateMessageService.class).generateMessage(event, guestAddedTemplate);
				if(message.getFromEmailAddress() == null){
					message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
				}
				message.addToEmailAddressSetItem(user.getPartner().getEmailAddress());
				message = Beans.get(MessageService.class).sendByEmail(message);
			}
		}

		Message message = Beans.get(TemplateMessageService.class).generateMessage(event, guestAddedTemplate);
		if(message.getFromEmailAddress() == null){
			message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
		}
		message.addToEmailAddressSetItem(event.getUser().getPartner().getEmailAddress());
		message = Beans.get(MessageService.class).sendByEmail(message);
	}
	
	@Transactional
	public void addUserGuest(User user, Event event) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		if(user.getPartner() != null && user.getPartner().getEmailAddress() != null){
			String email = user.getPartner().getEmailAddress().getAddress();
			if(event.getAttendees() != null && !Strings.isNullOrEmpty(email)){
				boolean exist = false;
				for (ICalendarUser attendee : event.getAttendees()) {
					if(email.equals(attendee.getEmail())){
						exist = true;
						break;
					}
				}
				if(!exist){
					ICalendarUser calUser = new ICalendarUser();
					calUser.setEmail(email);
					calUser.setName(user.getFullName());
					calUser.setUser(user);
					event.addAttendee(calUser);
					eventRepo.save(event);
					if(event.getCalendarCrm() != null){
						Beans.get(CalendarService.class).sync(event.getCalendarCrm());
					}
					this.sendMail(event, email);
				}
			}
		}
		else{
			throw new AxelorException(I18n.get("This user doesn't have any partner or email address"),IException.CONFIGURATION_ERROR);
		}
	}
	
	@Transactional
	public void addPartnerGuest(Partner partner, Event event) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		if(partner.getEmailAddress() != null){
			String email = partner.getEmailAddress().getAddress();
			if(event.getAttendees() != null && !Strings.isNullOrEmpty(email)){
				boolean exist = false;
				for (ICalendarUser attendee : event.getAttendees()) {
					if(email.equals(attendee.getEmail())){
						exist = true;
						break;
					}
				}
				if(!exist){
					ICalendarUser calUser = new ICalendarUser();
					calUser.setEmail(email);
					calUser.setName(partner.getFullName());
					if(partner.getUser() != null){
						calUser.setUser(partner.getUser());
					}
					event.addAttendee(calUser);
					eventRepo.save(event);
					if(event.getCalendarCrm() != null){
						Beans.get(CalendarService.class).sync(event.getCalendarCrm());
					}
					this.sendMail(event, email);
					
				}
			}
		}
		else{
			throw new AxelorException(I18n.get("This partner doesn't have any email address"),IException.CONFIGURATION_ERROR);
		}
	}
	
	@Transactional
	public void addEmailGuest(EmailAddress email, Event event) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		if(event.getAttendees() != null && email != null){
			boolean exist = false;
			for (ICalendarUser attendee : event.getAttendees()) {
				if(email.getAddress().equals(attendee.getEmail())){
					exist = true;
					break;
				}
			}
			if(!exist){
				ICalendarUser calUser = new ICalendarUser();
				calUser.setEmail(email.getAddress());
				calUser.setName(email.getName());
				if(email.getPartner() != null && email.getPartner().getUser() != null){
					calUser.setUser(email.getPartner().getUser());
				}
				event.addAttendee(calUser);
				eventRepo.save(event);
			}
		}
	}
	
	@Transactional
	public void sendMail(Event event, String email) throws AxelorException, MessagingException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ValidationException, ParseException, ICalendarException{
		EmailAddress emailAddress = null;
		emailAddress = Beans.get(EmailAddressRepository.class).all().filter("self.address = ?1", email).fetchOne();
		if(emailAddress == null){
			emailAddress = new EmailAddress(email);
		}
		Template guestAddedTemplate = Beans.get(CrmConfigService.class).getCrmConfig(event.getUser().getActiveCompany()).getMeetingGuestAddedTemplate();
		Message message = new Message();
		if(guestAddedTemplate == null){
			
			if(message.getFromEmailAddress() == null){
				message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
			}
			message.addToEmailAddressSetItem(emailAddress);
			message.setSubject(event.getSubject());
			message.setMailAccount(Beans.get(MailAccountService.class).getDefaultMailAccount());
		}
		else{
			message = Beans.get(TemplateMessageService.class).generateMessage(event, guestAddedTemplate);
			if(message.getFromEmailAddress() == null){
				message.setFromEmailAddress(event.getUser().getPartner().getEmailAddress());
			}
			message.addToEmailAddressSetItem(emailAddress);	
		}
		if(event.getUid() != null){
			CalendarService calendarService = Beans.get(CalendarService.class);
			Calendar cal = calendarService.getCalendar(event.getUid(), event.getCalendarCrm());
			cal.getProperties().add(Method.REQUEST);
			File file = calendarService.export(cal);
			Path filePath = file.toPath();
			MetaFile metaFile = new MetaFile();
			metaFile.setFileName( file.getName() );
			metaFile.setFileType( Files.probeContentType( filePath ) );
			metaFile.setFileSize( Files.size( filePath ) );
			metaFile.setFilePath( file.getName() );
			Set<MetaFile> fileSet = new HashSet<MetaFile>();
			fileSet.add(metaFile);
			Beans.get(MessageRepository.class).save(message);
			Beans.get(MessageService.class).attachMetaFiles(message, fileSet);
		}
		message = Beans.get(MessageService.class).sendByEmail(message);
	}
	
	@Transactional
	public void addRecurrentEventsByDays(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate){
		Event lastEvent = event;
		if(endType == 1){
			int repeated = 0;
			while(repeated != repetitionsNumber){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusDays(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusDays(periodicity));
				eventRepo.save(copy);
				repeated++;
				lastEvent = copy;
			}
		}
		else{
			while(!lastEvent.getStartDateTime().plusDays(periodicity).isAfter(endDate)){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusDays(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusDays(periodicity));
				eventRepo.save(copy);
				lastEvent = copy;
			}
		}
	}
	
	@Transactional
	public void addRecurrentEventsByWeeks(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate, Map<Integer, Boolean> daysCheckedMap){
		Event lastEvent = event;
		List<Integer> list = new ArrayList<Integer>();
		for (int day : daysCheckedMap.keySet()) {
			list.add(day);
		}
		Collections.sort(list);
		if(endType == 1){
			int repeated = 0;
			Event copy = eventRepo.copy(lastEvent, false);
			copy.setParentEvent(lastEvent);
			int dayOfWeek = copy.getStartDateTime().getDayOfWeek();
			LocalDateTime nextDateTime = new LocalDateTime();
			if(dayOfWeek < list.get(0)){
				nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek));
			}
			else if(dayOfWeek > list.get(0)){
				nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0)));
			}
			Duration dur = new Duration(copy.getStartDateTime().toDateTime(), copy.getEndDateTime().toDateTime());
			
			for (Integer integer : list) {
				nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek());
				copy.setStartDateTime(nextDateTime);
				copy.setEndDateTime(nextDateTime.plus(dur));
				eventRepo.save(copy);
				lastEvent = copy;
				repeated++;
			}
			
			while(repeated < repetitionsNumber){
				copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusWeeks(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusWeeks(periodicity));
				
				dayOfWeek = copy.getStartDateTime().getDayOfWeek();
				nextDateTime = new LocalDateTime();
				if(dayOfWeek < list.get(0)){
					nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek));
				}
				else if(dayOfWeek > list.get(0)){
					nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0)));
				}
				dur = new Duration(copy.getStartDateTime().toDateTime(), copy.getEndDateTime().toDateTime());
				
				for (Integer integer : list) {
					nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					lastEvent = copy;
					repeated++;
				}
			}
		}
		else{
			
			Event copy = eventRepo.copy(lastEvent, false);
			copy.setParentEvent(lastEvent);
			int dayOfWeek = copy.getStartDateTime().getDayOfWeek();
			LocalDateTime nextDateTime = new LocalDateTime();
			if(dayOfWeek < list.get(0)){
				nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek));
			}
			else if(dayOfWeek > list.get(0)){
				nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0)));
			}
			Duration dur = new Duration(copy.getStartDateTime().toDateTime(), copy.getEndDateTime().toDateTime());
			
			for (Integer integer : list) {
				nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek());
				copy.setStartDateTime(nextDateTime);
				copy.setEndDateTime(nextDateTime.plus(dur));
				eventRepo.save(copy);
				lastEvent = copy;
			}
			
			while(!copy.getStartDateTime().plusWeeks(periodicity).isAfter(endDate)){
				copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusWeeks(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusWeeks(periodicity));
				
				dayOfWeek = copy.getStartDateTime().getDayOfWeek();
				nextDateTime = new LocalDateTime();
				if(dayOfWeek < list.get(0)){
					nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek));
				}
				else if(dayOfWeek > list.get(0)){
					nextDateTime = new LocalDateTime(copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0)));
				}
				dur = new Duration(copy.getStartDateTime().toDateTime(), copy.getEndDateTime().toDateTime());
				
				for (Integer integer : list) {
					nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					lastEvent = copy;
				}
			}
		}
	}
	
	@Transactional
	public void addRecurrentEventsByMonths(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate, int monthRepeatType){
		Event lastEvent = event;
		if(monthRepeatType == 1){
			int dayOfMonth = event.getStartDateTime().getDayOfMonth();
			if(endType == 1){
				int repeated = 0;
				while(repeated != repetitionsNumber){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					if(copy.getStartDateTime().plusMonths(periodicity).dayOfMonth().getMaximumValue() >= dayOfMonth){
						copy.setStartDateTime(copy.getStartDateTime().plusMonths(periodicity));
						copy.setEndDateTime(copy.getEndDateTime().plusMonths(periodicity));
						eventRepo.save(copy);
						repeated++;
						lastEvent = copy;
					}
				}
			}
			else{
				while(!lastEvent.getStartDateTime().plusMonths(periodicity).isAfter(endDate)){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					if(copy.getStartDateTime().plusMonths(periodicity).dayOfMonth().getMaximumValue() >= dayOfMonth){
						copy.setStartDateTime(copy.getStartDateTime().plusMonths(periodicity));
						copy.setEndDateTime(copy.getEndDateTime().plusMonths(periodicity));
						eventRepo.save(copy);
						lastEvent = copy;
					}
				}
			}
		}
		
		else{
			int dayOfWeek = event.getStartDateTime().getDayOfWeek();
			int positionInMonth = 0;
			if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
				positionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
			}
			else{
				positionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
			}
			
			if(endType == 1){
				int repeated = 0;
				while(repeated != repetitionsNumber){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					LocalDateTime nextDateTime = new LocalDateTime(copy.getStartDateTime());
					nextDateTime.plusMonths(periodicity);
					int nextDayOfWeek = nextDateTime.getDayOfWeek();
					if(nextDayOfWeek > dayOfWeek){
						nextDateTime.minusDays(nextDayOfWeek - dayOfWeek);
					}
					else{
						nextDateTime.plusDays(dayOfWeek - nextDayOfWeek);
					}
					int nextPositionInMonth = 0;
					if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
						nextPositionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
					}
					else{
						nextPositionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
					}
					if(nextPositionInMonth > positionInMonth){
						nextDateTime.minusWeeks(nextPositionInMonth - positionInMonth);
					}
					else{
						nextDateTime.plusWeeks(positionInMonth - nextPositionInMonth);
					}
					Duration dur = new Duration(copy.getStartDateTime().toDateTime(), copy.getEndDateTime().toDateTime());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					repeated++;
					lastEvent = copy;
				}
			}
			else{
				LocalDateTime nextDateTime = new LocalDateTime(lastEvent.getStartDateTime());
				nextDateTime.plusMonths(periodicity);
				int nextDayOfWeek = nextDateTime.getDayOfWeek();
				if(nextDayOfWeek > dayOfWeek){
					nextDateTime.minusDays(nextDayOfWeek - dayOfWeek);
				}
				else{
					nextDateTime.plusDays(dayOfWeek - nextDayOfWeek);
				}
				int nextPositionInMonth = 0;
				if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
					nextPositionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
				}
				else{
					nextPositionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
				}
				if(nextPositionInMonth > positionInMonth){
					nextDateTime.minusWeeks(nextPositionInMonth - positionInMonth);
				}
				else{
					nextDateTime.plusWeeks(positionInMonth - nextPositionInMonth);
				}
				while(!nextDateTime.isAfter(endDate)){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					
					Duration dur = new Duration(copy.getStartDateTime().toDateTime(), copy.getEndDateTime().toDateTime());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					lastEvent = copy;
					
					nextDateTime = new LocalDateTime(lastEvent.getStartDateTime());
					nextDateTime.plusMonths(periodicity);
					nextDayOfWeek = nextDateTime.getDayOfWeek();
					if(nextDayOfWeek > dayOfWeek){
						nextDateTime.minusDays(nextDayOfWeek - dayOfWeek);
					}
					else{
						nextDateTime.plusDays(dayOfWeek - nextDayOfWeek);
					}
					nextPositionInMonth = 0;
					if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
						nextPositionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
					}
					else{
						nextPositionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
					}
					if(nextPositionInMonth > positionInMonth){
						nextDateTime.minusWeeks(nextPositionInMonth - positionInMonth);
					}
					else{
						nextDateTime.plusWeeks(positionInMonth - nextPositionInMonth);
					}
				}
			}
		}
	}
	
	@Transactional
	public void addRecurrentEventsByYears(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate){
		Event lastEvent = event;
		if(endType == 1){
			int repeated = 0;
			while(repeated != repetitionsNumber){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusYears(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusYears(periodicity));
				
				eventRepo.save(copy);
				repeated++;
				lastEvent = copy;
			}
		}
		else{
			while(!lastEvent.getStartDateTime().plusYears(periodicity).isAfter(endDate)){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusYears(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusYears(periodicity));
				eventRepo.save(copy);
				lastEvent = copy;
			}
		}
	}
	
	@Transactional
	public void applyChangesToAll(Event event){
		
		Event child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		Event parent = event.getParentEvent();
		Event copyEvent = eventRepo.copy(event, false);
		while(child != null){
			child.setSubject(event.getSubject());
			child.setCalendar(event.getCalendar());
			child.setStartDateTime(child.getStartDateTime().withHourOfDay(event.getStartDateTime().getHourOfDay()));
			child.setStartDateTime(child.getStartDateTime().withMinuteOfHour(event.getStartDateTime().getMinuteOfHour()));
			child.setEndDateTime(child.getEndDateTime().withHourOfDay(event.getEndDateTime().getHourOfDay()));
			child.setEndDateTime(child.getEndDateTime().withMinuteOfHour(event.getEndDateTime().getMinuteOfHour()));
			child.setDuration(event.getDuration());
			child.setUser(event.getUser());
			child.setTeam(event.getTeam());
			child.setDisponibilitySelect(event.getDisponibilitySelect());
			child.setVisibilitySelect(event.getVisibilitySelect());
			child.setDescription(event.getDescription());
			child.setClientPartner(event.getClientPartner());
			child.setContactPartner(event.getContactPartner());
			child.setLead(event.getLead());
			child.setTypeSelect(event.getTypeSelect());
			child.setLocation(event.getLocation());
			eventRepo.save(child);
			copyEvent = child;
			child = eventRepo.all().filter("self.parentEvent.id = ?1", copyEvent.getId()).fetchOne();
		}
		while(parent != null){
			Event nextParent = parent.getParentEvent();
			parent.setSubject(event.getSubject());
			parent.setCalendar(event.getCalendar());
			parent.setStartDateTime(parent.getStartDateTime().withHourOfDay(event.getStartDateTime().getHourOfDay()));
			parent.setStartDateTime(parent.getStartDateTime().withMinuteOfHour(event.getStartDateTime().getMinuteOfHour()));
			parent.setEndDateTime(parent.getEndDateTime().withHourOfDay(event.getEndDateTime().getHourOfDay()));
			parent.setEndDateTime(parent.getEndDateTime().withMinuteOfHour(event.getEndDateTime().getMinuteOfHour()));
			parent.setDuration(event.getDuration());
			parent.setUser(event.getUser());
			parent.setTeam(event.getTeam());
			parent.setDisponibilitySelect(event.getDisponibilitySelect());
			parent.setVisibilitySelect(event.getVisibilitySelect());
			parent.setDescription(event.getDescription());
			parent.setClientPartner(event.getClientPartner());
			parent.setContactPartner(event.getContactPartner());
			parent.setLead(event.getLead());
			parent.setTypeSelect(event.getTypeSelect());
			parent.setLocation(event.getLocation());
			eventRepo.save(parent);
			parent = nextParent;
		}
	}
	
	public String computeRecurrenceName(RecurrenceConfiguration recurrConf){
		String recurrName = "";
		switch (recurrConf.getRecurrenceType()) {
		case RecurrenceConfigurationRepository.TYPE_DAY:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every day");
			}
			else{
				recurrName += String.format(I18n.get("Every %d days"), recurrConf.getPeriodicity());
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(I18n.get(", %d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName += I18n.get(", until the ") + recurrConf.getEndDate().toString("dd/MM/yyyy");
			}
			break;
		
		case RecurrenceConfigurationRepository.TYPE_WEEK:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every week ");
			}
			else{
				recurrName += String.format(I18n.get("Every %d weeks "), recurrConf.getPeriodicity());
			}
			if(recurrConf.getMonday() && recurrConf.getTuesday() && recurrConf.getWednesday() && recurrConf.getThursday() && recurrConf.getFriday()
					&& !recurrConf.getSaturday() && !recurrConf.getSunday()){
				recurrName += I18n.get("every week's day");
			}
			else if(recurrConf.getMonday() && recurrConf.getTuesday() && recurrConf.getWednesday() && recurrConf.getThursday() && recurrConf.getFriday() 
					&& recurrConf.getSaturday() && recurrConf.getSunday()){
				recurrName += I18n.get("everyday");
			}
			else{
				recurrName += I18n.get("on ");
				if(recurrConf.getMonday()){
					recurrName += I18n.get("mon,");
				}
				if(recurrConf.getTuesday()){
					recurrName += I18n.get("tues,");
				}
				if(recurrConf.getWednesday()){
					recurrName += I18n.get("wed,");
				}
				if(recurrConf.getThursday()){
					recurrName += I18n.get("thur,");
				}
				if(recurrConf.getFriday()){
					recurrName += I18n.get("fri,");
				}
				if(recurrConf.getSaturday()){
					recurrName += I18n.get("sat,");
				}
				if(recurrConf.getSunday()){
					recurrName += I18n.get("sun,");
				}
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(I18n.get(" %d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName += I18n.get(" until the ") + recurrConf.getEndDate().toString("dd/MM/yyyy");
			}
			break;
		
		case RecurrenceConfigurationRepository.TYPE_MONTH:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every month the ") + recurrConf.getStartDate().getDayOfMonth();
			}
			else{
				recurrName += String.format(I18n.get("Every %d months the %d"), recurrConf.getPeriodicity(), recurrConf.getStartDate().getDayOfMonth());
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(I18n.get(", %d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName += I18n.get(", until the ") + recurrConf.getEndDate().toString("dd/MM/yyyy");
			}
			break;
			
		case RecurrenceConfigurationRepository.TYPE_YEAR:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every year the ") + recurrConf.getStartDate().toString("dd/MM");
			}
			else{
				recurrName += String.format(I18n.get("Every %d years the %s"), recurrConf.getPeriodicity(), recurrConf.getStartDate().toString("dd/MM"));
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(I18n.get(", %d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName += I18n.get(", until the ") + recurrConf.getEndDate().toString("dd/MM/yyyy");
			}
			break;

		default:
			break;
		}
		return recurrName;
	}
}
