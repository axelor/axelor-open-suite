/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.EmployeeComputeDaysLeaveBonusService;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class QuickTimesheetServiceImpl implements QuickTimesheetService {

  protected PublicHolidayService publicHolidayService;
  protected EmployeeComputeDaysLeaveBonusService employeeComputeDaysLeaveService;

  @Inject
  public QuickTimesheetServiceImpl(
      PublicHolidayService publicHolidayService,
      EmployeeComputeDaysLeaveBonusService employeeComputeDaysLeaveService) {

    this.publicHolidayService = publicHolidayService;
    this.employeeComputeDaysLeaveService = employeeComputeDaysLeaveService;
  }

  @Override
  public BigDecimal computeTotalTimeEntriesForPeriod(
      Employee employee, LocalDate fromDate, LocalDate toDate) {

    BigDecimal total = BigDecimal.ZERO;

    List<TimesheetLine> timesheetLineList =
        Query.of(TimesheetLine.class)
            .filter("self.employee = :employee AND self.date between :fromDate and :toDate")
            .bind("employee", employee)
            .bind("fromDate", fromDate)
            .bind("toDate", toDate)
            .fetch();

    if (CollectionUtils.isNotEmpty(timesheetLineList)) {
      total =
          timesheetLineList.stream()
              .map(TimesheetLine::getDuration)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return total.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal computeTotalLeavesAndHolidaysForPeriod(
      Employee employee, LocalDate fromDate, LocalDate toDate, String timeUnit)
      throws AxelorException {

    BigDecimal leaveDays =
        employeeComputeDaysLeaveService.computeDaysLeave(employee, fromDate, toDate);

    BigDecimal publicHolidays =
        publicHolidayService.computePublicHolidayDays(
            fromDate,
            toDate,
            employee.getWeeklyPlanning(),
            employee.getPublicHolidayEventsPlanning());

    return convertDayDurationForTimeUnit(employee, leaveDays.add(publicHolidays), timeUnit);
  }

  @Override
  public BigDecimal computeTotalWorkDurtionForPeriod(
      Employee employee, LocalDate fromDate, LocalDate toDate, String timeUnit)
      throws AxelorException {

    return convertDayDurationForTimeUnit(
        employee,
        employeeComputeDaysLeaveService.getDaysWorkedInPeriod(employee, fromDate, toDate),
        timeUnit);
  }

  protected BigDecimal convertDayDurationForTimeUnit(
      Employee employee, BigDecimal duration, String timeUnit) {

    BigDecimal dailyWorkHours = employee.getDailyWorkHours();

    switch (timeUnit) {
      case EmployeeRepository.TIME_PREFERENCE_HOURS:
        duration = duration.multiply(dailyWorkHours);
        break;
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        duration = duration.multiply(dailyWorkHours).multiply(BigDecimal.valueOf(60));
        break;
    }

    return duration.setScale(2, RoundingMode.HALF_UP);
  }
}
