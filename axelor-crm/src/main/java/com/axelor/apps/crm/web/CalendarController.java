/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.Team;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class CalendarController {
	
	private final Logger log =  LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	private CalendarService calendarService;
	
	@Inject
	private EventRepository eventRepo;
	
	public void showMyEvents(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();

		response.setView(ActionView
	            .define(I18n.get("My Calendar"))
	            .model(Event.class.getName())
				.add("calendar", "event-calendar-color-by-calendar")
	            .add("grid", "event-grid")
	            .add("form", "event-form")
	            .context("_typeSelect", 2)
	            .domain("self.user.id = "+ user.getId() +" or self.calendar.user.id = "+ user.getId() +" or self.attendees.user.id = "+ user.getId() +" or self.organizer.user.id = "+ user.getId())
	            .map());
	}
	
	public void showTeamEvents(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		Team team = user.getActiveTeam();
		List<Long> eventIdlist = new ArrayList<Long>();
		eventIdlist.add(new Long(0));
		
		Set<User> userSet = new HashSet<User>();
		if(team == null || team.getMembers() == null || team.getMembers().isEmpty()){
			userSet.add(user);
		}
		else{
			userSet = team.getMembers();
		}
		
		log.debug("Team members: {}", userSet);
		
		List<Event> eventList = eventRepo.all().filter("self.user in (?1) or self.calendar.user in (?1)"
				+ " or self.attendees.user in (?1) or self.organizer.user in (?1)",
				userSet).fetch();
		eventIdlist.addAll(Lists.transform(eventList, it->it.getId()));
		
		log.debug("Team event ids found: {}", eventIdlist);
		
		response.setView(ActionView
	            .define(I18n.get("Team Calendar"))
	            .model(Event.class.getName())
				.add("calendar", "event-calendar-color-by-user")
	            .add("grid", "event-grid")
	            .add("form", "event-form")
	            .context("_typeSelect", 2)
	            .context("_internalUser", user.getId())
	            .domain("self.team.id = "+ user.getActiveTeam().getId() +" and self.id in ("+Joiner.on(",").join(eventIdlist)+")")
	            .map());
	}
	
	public void showSharedEvents(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		List<Long> eventIdlist = calendarService.showSharedEvents(user);
		eventIdlist.add(new Long(0));
		
		log.debug("Shared event ids found: {}", eventIdlist);
		
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
	
}