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
package com.axelor.apps.crm.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.Method;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.EventRepository;
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
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

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
		Set<User> currentUsersSet = event.getInternalGuestSet();
		if(currentUsersSet != null){
			for (User user : currentUsersSet) {
				mailFollowerRepo.follow(event, user);
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
}
