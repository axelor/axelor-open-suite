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

import java.math.BigDecimal;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PlanningLineService {

	private static final Logger LOG = LoggerFactory.getLogger(PlanningLineService.class);
	
	@Inject
	private UnitConversionService unitConversionService;
	
	
	public int getMinutesDuration(Duration duration)  {
		
		int minutes = duration.toStandardMinutes().getMinutes() % 60;
		
		LOG.debug("Minutes : {}", minutes);	
		
		if(minutes >= 53 && minutes < 8)  {  return 00;  }
		else if(minutes >= 8 && minutes < 23)  {  return 15;  }
		else if(minutes >= 23 && minutes < 38)  {  return 30;  }
		else if(minutes >= 38 && minutes < 53)  {  return 45;  }
		
		return 00;
		
	}
	

	public LocalDateTime computeStartDateTime(BigDecimal duration, LocalDateTime endDateTime, Unit unit) throws AxelorException  {
			
		return endDateTime.minusMinutes(unitConversionService.convert(unit, Unit.all().filter("self.code = 'MIN'").fetchOne(), duration).intValue());
		
	}
	
	
	public LocalDateTime computeEndDateTime(LocalDateTime startDateTime, BigDecimal duration, Unit unit) throws AxelorException  {
		
		return startDateTime.plusMinutes(unitConversionService.convert(unit, Unit.all().filter("self.code = 'MIN'").fetchOne(), duration).intValue());
		
	}
	
	
	public BigDecimal getDuration(LocalDateTime startDateTime, LocalDateTime endDateTime, Unit unit) throws AxelorException  {
		
		return unitConversionService.convert(
				Unit.all().filter("self.code = 'MIN'").fetchOne(), 
				unit, 
				new BigDecimal(
						DurationTool.getMinutesDuration(
								DurationTool.computeDuration(startDateTime, endDateTime))));
		
	}
	
	
	public LocalDateTime getSpecificDateTime(LocalDateTime dateTime, int hour, int minute, int second, int millissecond)  {
		
		return dateTime.withTime(hour, minute, second, millissecond);		
	}


}
