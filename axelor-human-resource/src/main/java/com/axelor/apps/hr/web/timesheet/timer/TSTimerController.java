package com.axelor.apps.hr.web.timesheet.timer;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.Duration;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TSTimerRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
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
	
//	public void editTimesheetTimer(ActionRequest request, ActionResponse response){
//		List<TSTimer> tsTimerList = Beans.get(TSTimerRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
//		if(timesheetList.isEmpty()){
//			response.setView(ActionView
//									.define(I18n.get("Timesheet"))
//									.model(Timesheet.class.getName())
//									.add("form", "timesheet-form")
//									.map());
//		}
//		else if(timesheetList.size() == 1){
//			response.setView(ActionView
//					.define(I18n.get("Timesheet"))
//					.model(Timesheet.class.getName())
//					.add("form", "timesheet-form")
//					.param("forceEdit", "true")
//					.context("_showRecord", String.valueOf(timesheetList.get(0).getId())).map());
//		}
//	}
	
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
			Timesheet newTimesheet = timesheetServiceImp.getCurrentOrCreateTimesheet();
			TimesheetLine newTimesheetline = timesheetServiceImp.createTimesheetLine(timer.getProjectTask(), timer.getProduct(), timer.getUser(), timer.getStartTime().toLocalDate(), newTimesheet, minutes, timer.getComments());
			
			Beans.get(TimesheetRepository.class).save(newTimesheet);
			Beans.get(TimesheetLineRepository.class).save(newTimesheetline);
			timer.setAffectedToTimeSheetLine(newTimesheetline);
		}
		//Else the duration of the current timeSheetLine is update
		else{
			timer.getAffectedToTimeSheetLine().setDurationStored(minutes);
			timer.getAffectedToTimeSheetLine().setComments(timer.getComments());
		}
		TsTimerRepo.save(timer);
		response.setReload(true);
	}
	
}