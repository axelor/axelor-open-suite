/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
	
	
	
	
	
}
