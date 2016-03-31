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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.db.repo.ICalendarUserRepository;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.crm.db.Calendar;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.CalendarRepository;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.ValidationException;

public class CalendarController {

	@Inject
	private CalendarService calendarService;
	
	public void exportCalendar(ActionRequest request, ActionResponse response) throws IOException, ParserException, ValidationException, ObjectStoreException, ObjectNotFoundException, ParseException {
		Calendar cal = request.getContext().asType(Calendar.class);
		calendarService.export(cal);
	}
	
	public void importCalendarFile(ActionRequest request, ActionResponse response) throws IOException, ParserException
	{

		ImportConfiguration imp = request.getContext().asType(ImportConfiguration.class);
		Object object = request.getContext().get("_id");
		Calendar cal = null;
		if(object != null){
			Long id = Long.valueOf(object.toString());
			cal = Beans.get(CalendarRepository.class).find(id);
		}
		
		if(cal == null){
			cal = new Calendar();
		}
		
		
		File data = MetaFiles.getPath( imp.getDataMetaFile() ).toFile();

		calendarService.importCalendar(cal, data);
		response.setCanClose(true);
		response.setReload(true);
		
	}
	
	public void importCalendar(ActionRequest request, ActionResponse response) throws IOException, ParserException 
	{
		Calendar cal = request.getContext().asType(Calendar.class);
		response.setView(ActionView
					  		.define(I18n.get(IExceptionMessage.LEAD_5))
					  		.model("com.axelor.apps.base.db.ImportConfiguration")
					  		.add("form", "import-calendar-form")
					  		.param("popup", "reload")
					  		.param("forceEdit", "true")
					  		.param("show-toolbar", "false")
					  		.param("show-confirm", "false")
					  		.param("popup-save", "false")
					  		.context("_id", cal.getId())
					  		.map());
	}
	
	public void testConnect(ActionRequest request, ActionResponse response) throws Exception
	{
		Calendar cal = request.getContext().asType(Calendar.class);
		if (calendarService.testConnect(cal))
			response.setValue("isValid", true);
		else
			response.setAlert("Login and password do not match.");
		
	}
	
	public void showMyEvents(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		List<Long> eventIdlist = new ArrayList<Long>();
		List<ICalendarUser> userList = Beans.get(ICalendarUserRepository.class).all().filter("self.user.id = ?1", user.getId()).fetch();
		
		List<Event> eventList = Beans.get(EventRepository.class).all().filter("self.user.id = ?1",
				user.getId()).fetch();
		for (Event event : eventList) {
			eventIdlist.add(event.getId());
		}
		List<Calendar> calList = Beans.get(CalendarRepository.class).all().filter("self.user.id = ?1", user.getId()).fetch();
		for (Calendar calendar : calList) {
			for (Event event : calendar.getEventsCrm()) {
				eventIdlist.add(event.getId());
			}
		}
		for (ICalendarUser iCalendarUser : userList) {
			eventList = Beans.get(EventRepository.class).all().filter("?1 MEMBER OF self.attendees OR self.organizer.id = ?1", iCalendarUser.getId()).fetch();
			for (Event event : eventList) {
				eventIdlist.add(event.getId());
			}
		}
		response.setView(ActionView
	            .define(I18n.get("My Calendar"))
	            .model(Event.class.getName())
				.add("calendar", "event-calendar-color-by-calendar")
	            .add("grid", "event-grid")
	            .add("form", "event-form")
	            .context("_typeSelect", 2)
	            .domain("self.id in ("+Joiner.on(",").join(eventIdlist)+")")
	            .map());
	}
	
	public void showTeamEvents(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		Team team = user.getActiveTeam();
		List<Long> eventIdlist = new ArrayList<Long>();
		
		List<Event> eventList = null;
		
		Set<User> userSet = new HashSet<User>();
		if(team == null || team.getUserSet() == null || team.getUserSet().isEmpty()){
			userSet.add(user);
		}
		else{
			userSet = team.getUserSet();
		}
		
		for (User userIt : userSet) {
			List<ICalendarUser> userList = Beans.get(ICalendarUserRepository.class).all().filter("self.user.id = ?1", userIt.getId()).fetch();
			
			eventList = Beans.get(EventRepository.class).all().filter("self.user.id = ?1",
					userIt.getId()).fetch();
			for (Event event : eventList) {
				eventIdlist.add(event.getId());
			}
			List<Calendar> calList = Beans.get(CalendarRepository.class).all().filter("self.user.id = ?1", userIt.getId()).fetch();
			for (Calendar calendar : calList) {
				for (Event event : calendar.getEventsCrm()) {
					eventIdlist.add(event.getId());
				}
			}
			for (ICalendarUser iCalendarUser : userList) {
				eventList = Beans.get(EventRepository.class).all().filter("?1 MEMBER OF self.attendees OR self.organizer.id = ?1",
						iCalendarUser.getId()).fetch();
				for (Event event : eventList) {
					eventIdlist.add(event.getId());
				}
			}
		}
		
		response.setView(ActionView
	            .define(I18n.get("Team Calendar"))
	            .model(Event.class.getName())
				.add("calendar", "event-calendar-color-by-user")
	            .add("grid", "event-grid")
	            .add("form", "event-form")
	            .context("_typeSelect", 2)
	            .context("_internalUser", user.getId())
	            .domain("self.id in ("+Joiner.on(",").join(eventIdlist)+")")
	            .map());
	}
	
	public void showSharedEvents(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		List<Long> eventIdlist = calendarService.showSharedEvents(user);
		response.setView(ActionView
	            .define(I18n.get("Shared Calendar"))
	            .model(Event.class.getName())
				.add("calendar", "event-calendar-color-by-user")
	            .add("grid", "event-grid")
	            .add("form", "event-form")
	            .context("_typeSelect", 2)
	            .context("_internalUser", user.getId())
	            .domain("self.id in ("+Joiner.on(",").join(eventIdlist)+")")
	            .map());
	}
	
	public void synchronizeCalendar(ActionRequest request, ActionResponse response) throws MalformedURLException, SocketException, ObjectStoreException, ObjectNotFoundException, ConstraintViolationException, ICalendarException {
		Calendar cal = request.getContext().asType(Calendar.class);
		cal = Beans.get(CalendarRepository.class).find(cal.getId());
		calendarService.sync(cal);
		response.setReload(true);
	}

}



