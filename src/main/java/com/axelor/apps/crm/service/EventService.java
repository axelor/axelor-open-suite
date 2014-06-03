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
package com.axelor.apps.crm.service;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EventService {
	
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
		event.save();
	}
	
	
	@Transactional
	public void addLeadAttendee(Event event, Lead lead, Partner contactPartner)  {
		
		event.addEventAttendeeListItem(eventAttendeeService.createEventAttendee(event, lead, contactPartner));
		event.save();
		
	}
	
	
	@Transactional
	public void saveLead(Lead lead){
		lead.save();
	}
	
	
	
}
