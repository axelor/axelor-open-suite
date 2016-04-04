package com.axelor.apps.hr.web.timesheet.timer;

import java.math.BigDecimal;

import org.joda.time.Duration;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TSTimerRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TSTimerController{
	
	@Inject
	private EventService eventService;
	
	@Inject
	private GeneralService generalService;
	
	@Inject
	private TimesheetServiceImp timesheetServiceImp;
	
	@Inject
	private TSTimerRepository TsTimerRepo;
	
	@Transactional
	public void calculateDuration(ActionRequest request, ActionResponse response){
		TSTimer timerView = request.getContext().asType(TSTimer.class);
		TSTimer timer = TsTimerRepo.find(timerView.getId());
		
		long currentDuration = timer.getDuration();
		Duration duration = eventService.computeDuration(timer.getStartTime(), generalService.getTodayDateTime().toLocalDateTime());
		BigDecimal secondes = BigDecimal.valueOf((eventService.getDuration(duration) + currentDuration));
		timer.setDuration(secondes.longValue());
		BigDecimal minutes = BigDecimal.valueOf(secondes.longValue() / 60);
		
		//Creation of a new timesheetline if don't affected yet
		if(timer.getAffectedToTimeSheetLine() == null){
			Timesheet newTimeSheet = timesheetServiceImp.createTimesheet(timer.getUser(), timer.getStartTime().toLocalDate(), generalService.getTodayDate());
			TimesheetLine newTimeSheetline = timesheetServiceImp.createTimesheetLine(timer.getProjectTask(), timer.getProduct(), timer.getUser(), timer.getStartTime().toLocalDate(), newTimeSheet, minutes, timer.getComments());
			
			Beans.get(TimesheetRepository.class).save(newTimeSheet);
			Beans.get(TimesheetLineRepository.class).save(newTimeSheetline);
			timer.setAffectedToTimeSheetLine(newTimeSheetline);
		}
		//Else the duration of the current timeSheetLine is update
		else
			timer.getAffectedToTimeSheetLine().setDurationStored(minutes);
		
		response.setReload(true);
	}
	
}