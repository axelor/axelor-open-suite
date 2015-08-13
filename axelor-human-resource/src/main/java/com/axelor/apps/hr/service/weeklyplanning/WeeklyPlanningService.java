package com.axelor.apps.hr.service.weeklyplanning;

import org.joda.time.LocalDate;

import com.axelor.apps.hr.db.DayPlanning;
import com.axelor.apps.hr.db.WeeklyPlanning;
import com.axelor.exception.AxelorException;


public interface WeeklyPlanningService {
	public WeeklyPlanning initPlanning(WeeklyPlanning planning);
	public WeeklyPlanning checkPlanning(WeeklyPlanning planning) throws AxelorException;
	public double workingDayValue(WeeklyPlanning planning, LocalDate date);
	public DayPlanning findDayPlanning(WeeklyPlanning planning, LocalDate date);
	public DayPlanning findDayWithName(WeeklyPlanning planning, String name);
}
