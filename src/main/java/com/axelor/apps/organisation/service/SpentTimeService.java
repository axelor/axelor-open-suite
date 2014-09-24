/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.SpentTime;
import com.axelor.apps.base.db.repo.SpentTimeRepository;
import com.axelor.apps.organisation.db.TimesheetLine;


public class SpentTimeService extends SpentTimeRepository {


	public SpentTime createSpentTime (TimesheetLine timesheetLine)  {
		
		SpentTime spentTime = new SpentTime();
		spentTime.setDate(timesheetLine.getDate());
		spentTime.setTask(timesheetLine.getTask());
		BigDecimal duration = timesheetLine.getDuration();
		spentTime.setDuration(duration);
		spentTime.setUnit(timesheetLine.getTimesheet().getUnit());
		spentTime.setTimesheetImputed(true);
		spentTime.setUser(timesheetLine.getTimesheet().getUser());
		
		return spentTime;
		
	}
	
	
	
}
