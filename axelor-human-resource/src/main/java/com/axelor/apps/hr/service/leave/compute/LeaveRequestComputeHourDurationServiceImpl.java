/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.service.leave.LeaveRequestPlanningService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequestComputeHourDurationServiceImpl
    implements LeaveRequestComputeHourDurationService {

  protected final LeaveRequestPlanningService leaveRequestPlanningService;
  protected final PublicHolidayHrService publicHolidayHrService;
  protected final WeeklyPlanningService weeklyPlanningService;

  @Inject
  public LeaveRequestComputeHourDurationServiceImpl(
      LeaveRequestPlanningService leaveRequestPlanningService,
      PublicHolidayHrService publicHolidayHrService,
      WeeklyPlanningService weeklyPlanningService) {
    this.leaveRequestPlanningService = leaveRequestPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  /**
   * Computes the duration in hours of a leave, according to the weekly and the holiday plannings.
   *
   * @param leave
   * @param employee
   * @param fromDateT
   * @param toDateT
   * @return
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDurationInHours(
      LeaveRequest leave, Employee employee, LocalDateTime fromDateT, LocalDateTime toDateT)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning = leaveRequestPlanningService.getWeeklyPlanning(leave, employee);
    EventsPlanning holidayPlanning =
        leaveRequestPlanningService.getPublicHolidayEventsPlanning(leave, employee);
    LocalDate fromDate = fromDateT.toLocalDate();
    LocalDate toDate = toDateT.toLocalDate();

    if (toDate.equals(fromDate)
        && !publicHolidayHrService.checkPublicHolidayDay(fromDate, holidayPlanning)) {
      duration =
          duration.add(
              weeklyPlanningService.getWorkingDayValueInHours(
                  weeklyPlanning, fromDate, fromDateT.toLocalTime(), toDateT.toLocalTime()));

    } else {
      // First day of leave
      if (!publicHolidayHrService.checkPublicHolidayDay(fromDate, holidayPlanning)) {
        duration =
            duration.add(
                weeklyPlanningService.getWorkingDayValueInHours(
                    weeklyPlanning, fromDate, fromDateT.toLocalTime(), null));
      }
      fromDate = fromDate.plusDays(1);

      // Last day of leave
      if (!publicHolidayHrService.checkPublicHolidayDay(toDate, holidayPlanning)) {
        duration =
            duration.add(
                weeklyPlanningService.getWorkingDayValueInHours(
                    weeklyPlanning, toDate, null, toDateT.toLocalTime()));
      }

      // Daily leave duration of the other days between from and to date
      for (LocalDate date = fromDate; date.isBefore(toDate); date = date.plusDays(1)) {
        if (!publicHolidayHrService.checkPublicHolidayDay(date, holidayPlanning)) {
          duration =
              duration.add(
                  weeklyPlanningService.getWorkingDayValueInHours(
                      weeklyPlanning, date, null, null));
        }
      }
    }

    return duration;
  }
}
