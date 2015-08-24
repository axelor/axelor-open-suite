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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.config.CrmConfigService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EventService extends EventRepository {

	@Inject
	private EventAttendeeService eventAttendeeService;

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
		save(event);
	}


	@Transactional
	public void addLeadAttendee(Event event, Lead lead, Partner contactPartner)  {

		event.addEventAttendeeListItem(eventAttendeeService.createEventAttendee(event, lead, contactPartner));
		save(event);

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
}
