package com.axelor.apps.base.service.weeklyplanning;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.exception.AxelorException;


public interface WeeklyPlanningService {
	public WeeklyPlanning initPlanning(WeeklyPlanning planning);
	public WeeklyPlanning checkPlanning(WeeklyPlanning planning) throws AxelorException;
	public double workingDayValue(WeeklyPlanning planning, LocalDate date);
	public DayPlanning findDayPlanning(WeeklyPlanning planning, LocalDate date);
	public DayPlanning findDayWithName(WeeklyPlanning planning, String name);
}
