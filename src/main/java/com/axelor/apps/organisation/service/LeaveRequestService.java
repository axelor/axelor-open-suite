/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
package com.axelor.apps.organisation.service;


import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.organisation.db.IEvent;
import com.axelor.apps.organisation.db.LeaveRequest;
import com.google.inject.persist.Transactional;

public class LeaveRequestService {

	private static final Logger LOG = LoggerFactory.getLogger(LeaveRequestService.class);
	
	@Transactional
	public Event createHolidayEvent(LeaveRequest leaveRequest)  {
		
		Event event = new Event();
		event.setTypeSelect(IEvent.HOLIDAY);
		event.setStartDateTime(leaveRequest.getStartDateT());
		event.setEndDateTime(leaveRequest.getEndDateT());
		event.setUserInfo(leaveRequest.getEmployeeUserInfo());
		if(leaveRequest.getReasonTask()!= null)  {
			event.setDescription(leaveRequest.getReasonTask().getName());
		}
		return event.save();
		
	}
	
	public Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime)  {
		
		return new Interval(startDateTime.toDateTime(), endDateTime.toDateTime()).toDuration();
	}
	
	public int getHoursDuration(Duration duration)  {
		
		return duration.toStandardHours().getHours();
	}
	
	public int getDaysDuration(int hours)  {
		
		double day = (double)hours/(double)24;
		double arrondi = Math.ceil(day);
		
		return (int) arrondi;
	}

	public int getMinutesDuration(Duration duration)  {
		
		int minutes = duration.toStandardMinutes().getMinutes() % 60;
		
		LOG.debug("Minutes : {}", minutes);	
		
		if(minutes >= 53 && minutes < 8)  {  return 00;  }
		else if(minutes >= 8 && minutes < 23)  {  return 15;  }
		else if(minutes >= 23 && minutes < 38)  {  return 30;  }
		else if(minutes >= 38 && minutes < 53)  {  return 45;  }
		
		return 00;
		
	}

	public LocalDateTime computeStartDateTime(double duration, LocalDateTime endDateTime)  {
			
		return endDateTime.minusHours((int)duration*24);	
		
	}
	
	public LocalDateTime computeEndDateTime(LocalDateTime startDateTime, double durationDay)  {
		
		return startDateTime.plusHours((int)durationDay*24);
		
	}
}
