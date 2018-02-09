/*
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
package com.axelor.apps.crm.service;

import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class EventReminderService {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	public Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime)  {
		
		return Duration.between(startDateTime, endDateTime);
		
	}
	
	public Integer getHoursDuration(Duration duration)  {
		
		return new Integer(new Long(duration.toHours()).toString());
		
	}
	
	public int getMinutesDuration(Duration duration)  {
		
		int minutes = new Integer(new Long(duration.toMinutes()).toString()) % 60;
		
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
	
	
}
