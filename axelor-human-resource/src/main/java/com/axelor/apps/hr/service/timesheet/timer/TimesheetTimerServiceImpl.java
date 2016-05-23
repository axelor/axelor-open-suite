package com.axelor.apps.hr.service.timesheet.timer;

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
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TimesheetTimerServiceImpl implements TimesheetTimerService {
	
	@Inject
	protected EventService eventService;
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected TimesheetService timesheetService;
	
	@Transactional(rollbackOn = {Exception.class})
	public void pause(TSTimer timer){
		timer.setStatusSelect(TSTimerRepository.STATUS_PAUSE);
		calculateDuration(timer);
	}
	
	@Transactional(rollbackOn = {Exception.class})
	public void stop(TSTimer timer) {
		timer.setStatusSelect(TSTimerRepository.STATUS_STOP);
		calculateDuration(timer);
		if(timer.getDuration() > 59)
			generateTimesheetLine(timer);
	}
	
	@Transactional(rollbackOn = {Exception.class})
	public void calculateDuration(TSTimer timer){
		long currentDuration = timer.getDuration();
		Duration duration = eventService.computeDuration(timer.getStartDateTime(), generalService.getTodayDateTime().toLocalDateTime());
		BigDecimal secondes = BigDecimal.valueOf((eventService.getDuration(duration) + currentDuration));
		timer.setDuration(secondes.longValue());
	}

	@Transactional(rollbackOn = {Exception.class})
	public TimesheetLine generateTimesheetLine(TSTimer timer) {
		BigDecimal durationHours = BigDecimal.valueOf(timer.getDuration() / 3600);
		Timesheet timesheet = timesheetService.getCurrentOrCreateTimesheet();
		TimesheetLine timesheetLine = timesheetService.createTimesheetLine(timer.getProjectTask(), timer.getProduct(), timer.getUser(), timer.getStartDateTime().toLocalDate(), timesheet, durationHours, timer.getComments());
		
		Beans.get(TimesheetRepository.class).save(timesheet);
		Beans.get(TimesheetLineRepository.class).save(timesheetLine);
		timer.setTimeSheetLine(timesheetLine);
		
		return timesheetLine;
	}
	
	public TSTimer getCurrentTSTimer(){
		return Beans.get(TSTimerRepository.class).all().filter("self.user = ?1",AuthUtils.getUser()).fetchOne();
	}
	
}
