package com.axelor.apps.hr.db.repo;

import java.math.BigDecimal;

import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class TimesheetTimerHRRepository extends TSTimerRepository{

	@Inject
	private TimesheetTimerService tsTimerService;
	
	@Override
	public TSTimer save(TSTimer tsTimer){
		if(tsTimer.getStatusSelect() == TSTimerRepository.STATUS_STOP){
			if(tsTimer.getTimeSheetLine() != null)
				updateTimesheetLine(tsTimer);
			else
				tsTimerService.generateTimesheetLine(tsTimer);
		}
		
		return super.save(tsTimer);
	}
	
	public void updateTimesheetLine(TSTimer tsTimer){
		TimesheetLine timesheetLine = tsTimer.getTimeSheetLine();
		
		timesheetLine.setProjectTask(tsTimer.getProjectTask());
		timesheetLine.setProduct(tsTimer.getProduct());
		timesheetLine.setDurationStored(BigDecimal.valueOf(tsTimer.getDuration() / 3600));
		timesheetLine.setComments(tsTimer.getComments());
		
		Beans.get(TimesheetLineRepository.class).save(timesheetLine);
	}
	
}
