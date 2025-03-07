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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leave.LeaveRequestPlanningService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LeaveRequestComputeDayDurationServiceImpl
    implements LeaveRequestComputeDayDurationService {

  protected final LeaveRequestPlanningService leaveRequestPlanningService;
  protected final LeaveRequestComputeHalfDayService leaveRequestComputeHalfDayService;
  protected final WeeklyPlanningService weeklyPlanningService;
  protected final PublicHolidayHrService publicHolidayHrService;

  @Inject
  public LeaveRequestComputeDayDurationServiceImpl(
      LeaveRequestPlanningService leaveRequestPlanningService,
      LeaveRequestComputeHalfDayService leaveRequestComputeHalfDayService,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService publicHolidayHrService) {
    this.leaveRequestPlanningService = leaveRequestPlanningService;
    this.leaveRequestComputeHalfDayService = leaveRequestComputeHalfDayService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
  }

  /**
   * Computes the duration in days of a leave, according to the input planning.
   *
   * @param leave
   * @param employee
   * @param fromDate
   * @param toDate
   * @param startOn
   * @param endOn
   * @return
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDurationInDays(
      LeaveRequest leave,
      Employee employee,
      LocalDate fromDate,
      LocalDate toDate,
      int startOn,
      int endOn)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning = leaveRequestPlanningService.getWeeklyPlanning(leave, employee);
    EventsPlanning holidayPlanning =
        leaveRequestPlanningService.getPublicHolidayEventsPlanning(leave, employee);

    return computeDurationInDays(
        fromDate, toDate, startOn, endOn, duration, weeklyPlanning, holidayPlanning);
  }

  @Override
  public BigDecimal computeDurationInDays(
      Company company,
      Employee employee,
      LocalDate fromDate,
      LocalDate toDate,
      int startOn,
      int endOn)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning =
        leaveRequestPlanningService.getWeeklyPlanning(employee, company);
    EventsPlanning holidayPlanning =
        leaveRequestPlanningService.getPublicHolidayEventsPlanning(employee, company);

    return computeDurationInDays(
        fromDate, toDate, startOn, endOn, duration, weeklyPlanning, holidayPlanning);
  }

  protected BigDecimal computeDurationInDays(
      LocalDate fromDate,
      LocalDate toDate,
      int startOn,
      int endOn,
      BigDecimal duration,
      WeeklyPlanning weeklyPlanning,
      EventsPlanning holidayPlanning) {

    if (fromDate == null || toDate == null || startOn == 0 || endOn == 0) {
      return BigDecimal.ZERO;
    }

    // If the leave request is only for 1 day
    if (fromDate.isEqual(toDate)) {
      if (startOn == endOn) {
        if (startOn == LeaveRequestRepository.SELECT_MORNING) {
          duration =
              duration.add(
                  BigDecimal.valueOf(
                      weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                          weeklyPlanning, fromDate, true, false)));
        } else {
          duration =
              duration.add(
                  BigDecimal.valueOf(
                      weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                          weeklyPlanning, fromDate, false, true)));
        }
      } else {
        duration =
            duration.add(
                BigDecimal.valueOf(
                    weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                        weeklyPlanning, fromDate, true, true)));
      }

      // Else if it's on several days
    } else {
      duration =
          duration.add(
              BigDecimal.valueOf(
                  leaveRequestComputeHalfDayService.computeStartDateWithSelect(
                      fromDate, startOn, weeklyPlanning)));

      LocalDate itDate = fromDate.plusDays(1);
      while (!itDate.isEqual(toDate) && !itDate.isAfter(toDate)) {
        duration =
            duration.add(
                BigDecimal.valueOf(
                    weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, itDate)));
        itDate = itDate.plusDays(1);
      }

      duration =
          duration.add(
              BigDecimal.valueOf(
                  leaveRequestComputeHalfDayService.computeEndDateWithSelect(
                      toDate, endOn, weeklyPlanning)));
    }

    if (holidayPlanning != null) {
      duration =
          duration.subtract(
              publicHolidayHrService.computePublicHolidayDays(
                  fromDate, toDate, weeklyPlanning, holidayPlanning));
    }

    return duration;
  }
}
