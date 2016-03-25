package com.axelor.apps.hr.web.timesheet.timer;

import org.joda.time.Duration;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TSTimerController{
	
	@Inject
	private EventService eventService;
	
	@Inject
	private GeneralService generalService;
	
	public void calculateDuration(ActionRequest request, ActionResponse response){
		TSTimer timer = request.getContext().asType(TSTimer.class);

		long currentDuration = timer.getDuration();
		Duration duration = eventService.computeDuration(timer.getStartTime(), generalService.getTodayDateTime().toLocalDateTime());
		response.setValue("duration", eventService.getDuration(duration) + currentDuration);
		
		
		//creation of a new timesheetline if don't affected yet
		TimesheetLine timesheetline = new TimesheetLine();
		
		//timesheetline.setDurationStored(timer.getDuration());
		timesheetline.setProjectTask(timer.getProjectTask());
	}
	
}