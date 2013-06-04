package com.axelor.apps.organisation.service;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanningLineService {

	private static final Logger LOG = LoggerFactory.getLogger(PlanningLineService.class);
	
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

	public LocalDateTime computeStartDateTime(int duration, LocalDateTime endDateTime)  {
			
		return endDateTime.minusHours(duration*24);	
		
	}
	
	public LocalDateTime computeEndDateTime(LocalDateTime startDateTime, int durationDay)  {
		
		return startDateTime.plusHours(durationDay*24);
		
	}
	
	
}
