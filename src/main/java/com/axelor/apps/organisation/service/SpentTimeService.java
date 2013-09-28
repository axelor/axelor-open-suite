/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.SpentTime;
import com.axelor.apps.organisation.db.TimesheetLine;


public class SpentTimeService {


	public SpentTime createSpentTime (TimesheetLine timesheetLine)  {
		
		SpentTime spentTime = new SpentTime();
		spentTime.setDate(timesheetLine.getDate());
		spentTime.setTask(timesheetLine.getTask());
		BigDecimal duration = timesheetLine.getDuration();
		spentTime.setDurationHours(duration.intValue());
		spentTime.setDurationMinutesSelect(this.getMinutesDuration(duration));
		spentTime.setTimesheetImputed(true);
		spentTime.setUserInfo(timesheetLine.getTimesheet().getUserInfo());
		
		return spentTime;
		
	}
	
	
	public int getMinutesDuration(BigDecimal duration)  {
		
		int minutes = duration.subtract(duration.setScale(0)).multiply(new BigDecimal(60)).intValue();
		
		if(minutes >= 53 && minutes < 8)  {  return 00;  }
		else if(minutes >= 8 && minutes < 23)  {  return 15;  }
		else if(minutes >= 23 && minutes < 38)  {  return 30;  }
		else if(minutes >= 38 && minutes < 53)  {  return 45;  }
		
		return 00;
		
	}
}
