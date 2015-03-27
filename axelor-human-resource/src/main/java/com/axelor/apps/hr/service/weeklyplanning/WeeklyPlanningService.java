package com.axelor.apps.hr.service.weeklyplanning;

import com.axelor.apps.hr.db.WeeklyPlanning;
import com.axelor.exception.AxelorException;


public interface WeeklyPlanningService {
	public WeeklyPlanning initPlanning(WeeklyPlanning planning);
	public WeeklyPlanning checkPlanning(WeeklyPlanning planning) throws AxelorException;
}
