package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.PastTime;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.db.TimesheetInput;
import com.axelor.apps.organisation.db.TimesheetLine;
import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;

public class TimesheetService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TimesheetService.class); 
	
	@Transactional
	public void getTaskPastTime(Timesheet timesheet)  {
		
		UserInfo userInfo = timesheet.getUserInfo();
		
		Query q = JPA.em().createQuery("select DISTINCT(task) FROM PastTime as pt WHERE pt.userInfo = ?1 AND pt.timesheetImputed IN (false,null)");
		q.setParameter(1, userInfo);
				
		@SuppressWarnings("unchecked")
		List<Task> taskList = q.getResultList();
		
		for(Task task : taskList)  {
			
			timesheet.addTimesheetLineListItem(this.createTimesheetLine(timesheet, task));
			
		}
		timesheet.save();
		
	}
	
	public TimesheetLine createTimesheetLine(Timesheet timesheet, Task task)  {
		
		TimesheetLine timesheetLine = new TimesheetLine();
		timesheetLine.setTimesheet(timesheet);
		
		timesheetLine.setProject(task.getProject());
		timesheetLine.setTask(task);
		
		List<PastTime> pastTimeList = PastTime.all().filter("self.userInfo = ?1 AND self.task = ?2  AND pt.timesheetImputed IN (false,null)", timesheet.getUserInfo(), task).fetch();
		
		for(PastTime pastTime : pastTimeList)  {
			
			timesheetLine.addTimesheetInputListItem(this.createTimesheetInput(timesheetLine, pastTime));
			
			pastTime.setTimesheetImputed(true);
			pastTime.save();
		}
		
		return timesheetLine;
	}
	
	
	public TimesheetInput createTimesheetInput(TimesheetLine timesheetLine, PastTime pastTime)  {
		
		TimesheetInput timesheetInput = new TimesheetInput();
		timesheetInput.setTimesheetLine(timesheetLine);
		timesheetInput.setDate(pastTime.getDate());
		timesheetInput.setIsToInvoice(true);
		
		int duration = pastTime.getDurationHours()+(pastTime.getDurationMinutesSelect()/60);
		timesheetInput.setDuration(new BigDecimal(duration));
		
		return timesheetInput;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
