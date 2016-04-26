package com.axelor.apps.hr.web.timesheet.timer;

import java.util.List;

import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.repo.TSTimerRepository;
import com.axelor.apps.hr.service.timesheet.timer.TimesheetTimerService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TSTimerController{
	
	@Inject
	private TSTimerRepository TsTimerRepo;
	
	@Inject
	private TimesheetTimerService tsTimerService;
	
	public void editTimesheetTimer(ActionRequest request, ActionResponse response){
		List<TSTimer> tsTimerList = Beans.get(TSTimerRepository.class).all().filter("self.user = ?1",AuthUtils.getUser()).fetch();
		if(tsTimerList.isEmpty()){
			response.setView(ActionView
									.define(I18n.get("TSTimer"))
									.model(TSTimer.class.getName())
									.add("form", "ts-timer-form")
									.map());
		}
		else{
			response.setView(ActionView
					.define(I18n.get("TSTimer"))
					.model(TSTimer.class.getName())
					.add("form", "ts-timer-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(tsTimerList.get(0).getId())).map());
		}
	}
	
	public void pause(ActionRequest request, ActionResponse response){
		TSTimer timerView = request.getContext().asType(TSTimer.class);
		TSTimer timer = TsTimerRepo.find(timerView.getId());
		
		tsTimerService.pause(timer);
		
		response.setReload(true);
	}
	
	public void stop(ActionRequest request, ActionResponse response){
		TSTimer timerView = request.getContext().asType(TSTimer.class);
		TSTimer timer = TsTimerRepo.find(timerView.getId());
		
		tsTimerService.stop(timer);
		
		if(timer.getDuration() < 60)
			response.setFlash(I18n.get("No timesheet line has been created because the duration is less than 1 minute"));
		response.setReload(true);
	}
	
}