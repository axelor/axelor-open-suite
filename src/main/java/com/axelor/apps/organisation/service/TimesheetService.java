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
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.SpentTime;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.db.TimesheetLine;
import com.axelor.db.JPA;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;

public class TimesheetService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TimesheetService.class); 
	
	@Transactional
	public void getTaskSpentTime(Timesheet timesheet)  {
		
		UserInfo userInfo = timesheet.getUserInfo();
		
		Query q = JPA.em().createQuery("select DISTINCT(task) FROM SpentTime as pt WHERE pt.userInfo = ?1 AND pt.timesheetImputed IN (false,null)");
		q.setParameter(1, userInfo);
				
		@SuppressWarnings("unchecked")
		List<Task> taskList = q.getResultList();
		
		for(Task task : taskList)  {
			
			timesheet.getTimesheetLineList().addAll(this.createTimesheetLines(timesheet, task));
			
		}
		timesheet.save();
		
	}
	
	public List<TimesheetLine> createTimesheetLines(Timesheet timesheet, Task task)  {
		
		List<TimesheetLine> timesheetLineList = Lists.newArrayList();
		
		List<SpentTime> spentTimeList = SpentTime.all().filter("self.userInfo = ?1 AND self.task = ?2  AND self.timesheetImputed IN (false,null)", timesheet.getUserInfo(), task).fetch();
		
		for(SpentTime spentTime : spentTimeList)  {
			
			timesheetLineList.add(this.createTimesheetLine(timesheet, task, spentTime));
			
			spentTime.setTimesheetImputed(true);
			spentTime.save();
		}
		
		return timesheetLineList;
	}
	
	
	public TimesheetLine createTimesheetLine(Timesheet timesheet, Task task, SpentTime spentTime)  {
		
		TimesheetLine timesheetLine = new TimesheetLine();
		timesheetLine.setTimesheet(timesheet);
		
		timesheetLine.setProject(task.getProject());
		timesheetLine.setTask(task);
		timesheetLine.setIsToInvoice(true);
		timesheetLine.setDate(spentTime.getDate());
		
		int duration = spentTime.getDurationHours()+(spentTime.getDurationMinutesSelect()/60);
		timesheetLine.setDuration(new BigDecimal(duration));
		
		return timesheetLine;
		
	}
	
	
}
