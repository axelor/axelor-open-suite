package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.google.inject.Inject;
import java.time.LocalDate;

public class WorkingDayServiceImpl implements WorkingDayService {
  protected final WeeklyPlanningService weeklyPlanningService;
  protected final PublicHolidayHrService publicHolidayHrService;

  @Inject
  public WorkingDayServiceImpl(
      WeeklyPlanningService weeklyPlanningService, PublicHolidayHrService publicHolidayHrService) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
  }

  @Override
  public boolean isWorkingDay(Employee employee, LocalDate date) {
    WeeklyPlanning planning = employee.getWeeklyPlanning();
    return isWorkingDay(weeklyPlanningService.findDayPlanning(planning, date))
        && !publicHolidayHrService.checkPublicHolidayDay(date, employee);
  }

  protected boolean isWorkingDay(DayPlanning dayPlanning) {
    if (dayPlanning == null) {
      return false;
    }
    return dayPlanning.getMorningFrom() != null
        || dayPlanning.getMorningTo() != null
        || dayPlanning.getAfternoonFrom() != null
        || dayPlanning.getAfternoonTo() != null;
  }
}
