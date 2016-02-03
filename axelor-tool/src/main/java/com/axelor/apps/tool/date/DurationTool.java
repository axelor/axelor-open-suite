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
package com.axelor.apps.tool.date;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

public class DurationTool{
	
	public static Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime)  {
		
		return new Interval(startDateTime.toDateTime(), endDateTime.toDateTime()).toDuration();
	}
	
	
	public static int getDaysDuration(Duration duration)  {
		
		return duration.toStandardDays().getDays();
	}
	
	public static int getHoursDuration(Duration duration)  {
		
		return duration.toStandardHours().getHours();
	}
	
	public static int getMinutesDuration(Duration duration)  {
		
		return duration.toStandardMinutes().getMinutes();
	}
	
}
