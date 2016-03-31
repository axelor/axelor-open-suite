package com.axelor.apps.hr.web.timesheet.timer;

import java.math.BigDecimal;

import org.joda.time.Duration;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TSTimerController{
	
	@Inject
	private EventService eventService;
	
	@Inject
	private GeneralService generalService;
	
	@Inject
	private TimesheetServiceImp timesheetServiceImp;
	
	public void calculateDuration(ActionRequest request, ActionResponse response){
		TSTimer timer = request.getContext().asType(TSTimer.class);

		long currentDuration = timer.getDuration();
		Duration duration = eventService.computeDuration(timer.getStartTime(), generalService.getTodayDateTime().toLocalDateTime());
		BigDecimal minutes = BigDecimal.valueOf((eventService.getDuration(duration) + currentDuration) / 60);
		response.setValue("duration", minutes);
		
		
		//creation of a new timesheetline if don't affected yet
		if(timer.getAffectedToTimeSheetLine() == null)
			timesheetServiceImp.createTimesheetLine(project, product, user, date, timesheet, minutes, comment);
		//Else the duration of the current timeSheetLine is update
		else
			timer.getAffectedToTimeSheetLine().setDurationStored(minutes);
	}
}