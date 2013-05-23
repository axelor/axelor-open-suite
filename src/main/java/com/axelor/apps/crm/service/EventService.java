package com.axelor.apps.crm.service;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.db.LeaveRequest;
import com.google.inject.persist.Transactional;

public class EventService {

	private static final Logger LOG = LoggerFactory.getLogger(EventService.class);
	
	public Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime)  {
		
		return new Interval(startDateTime.toDateTime(), endDateTime.toDateTime()).toDuration();
		
	}
	
	public int getHoursDuration(Duration duration)  {
		
		return duration.toStandardHours().getHours();
		
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
	
	public LocalDateTime computeStartDateTime(int durationHours, int durationMinutes, LocalDateTime endDateTime)  {
			
		return endDateTime.minusHours(durationHours).minusMinutes(durationMinutes);	
		
	}
	
	public LocalDateTime computeEndDateTime(LocalDateTime startDateTime, int durationHours, int durationMinutes)  {
		
		return startDateTime.plusHours(durationHours).plusMinutes(durationMinutes);	
	}
	
	
	@Transactional
	public Event createHolidayEvent(LeaveRequest leaveRequest)  {
		
		Event event = new Event();
		event.setTypeSelect(IEvent.HOLIDAY);
		event.setStartDateTime(leaveRequest.getStartDateT());
		event.setEndDateTime(leaveRequest.getEndDateT());
		
		Employee employee = leaveRequest.getEmployee();
		event.setDescription(employee.getName()+" "+employee.getFirstName());
		return event.save();
		
	}
	
	
	
}
