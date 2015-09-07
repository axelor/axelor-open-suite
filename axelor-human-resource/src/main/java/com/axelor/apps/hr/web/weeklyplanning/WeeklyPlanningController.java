package com.axelor.apps.hr.web.weeklyplanning;

import com.axelor.apps.hr.db.WeeklyPlanning;
import com.axelor.apps.hr.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class WeeklyPlanningController {
	
	@Inject
	private WeeklyPlanningService weeklyPlanningService;
	
	public void initPlanning(ActionRequest request, ActionResponse response){
		WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
		planning = weeklyPlanningService.initPlanning(planning);
		response.setValue("weekDays",planning.getWeekDays());
	}
	
	public void checkPlanning(ActionRequest request, ActionResponse response) throws AxelorException{
		WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
		planning = weeklyPlanningService.checkPlanning(planning);	
	}
}
