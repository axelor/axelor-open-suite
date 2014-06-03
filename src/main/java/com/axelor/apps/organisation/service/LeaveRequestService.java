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
