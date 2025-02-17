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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeLeaveHoursService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppTimesheet;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class TimesheetLeaveServiceImpl implements TimesheetLeaveService {

  protected AppHumanResourceService appHumanResourceService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected PublicHolidayService publicHolidayService;
  protected LeaveRequestService leaveRequestService;
  protected LeaveRequestComputeLeaveHoursService leaveRequestComputeLeaveHoursService;
  protected TimesheetLineCreateService timesheetLineCreateService;

  @Inject
  public TimesheetLeaveServiceImpl(
      AppHumanResourceService appHumanResourceService,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayService publicHolidayService,
      LeaveRequestService leaveRequestService,
      LeaveRequestComputeLeaveHoursService leaveRequestComputeLeaveHoursService,
      TimesheetLineCreateService timesheetLineCreateService) {
    this.appHumanResourceService = appHumanResourceService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayService = publicHolidayService;
    this.leaveRequestService = leaveRequestService;
    this.leaveRequestComputeLeaveHoursService = leaveRequestComputeLeaveHoursService;
    this.timesheetLineCreateService = timesheetLineCreateService;
  }

  @Override
  public void prefillLines(Timesheet timesheet) throws AxelorException {
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();

    Employee employee = timesheet.getEmployee();
    Company company = timesheet.getCompany();
    WeeklyPlanning weeklyPlanning =
        employee != null ? employee.getWeeklyPlanning() : company.getWeeklyPlanning();
    EventsPlanning holidayPlanning =
        employee != null
            ? employee.getPublicHolidayEventsPlanning()
            : company.getPublicHolidayEventsPlanning();
    removeGeneratedLines(timesheet);
    createTimesheetLines(timesheet, fromDate, toDate, weeklyPlanning, holidayPlanning, employee);
  }

  protected void removeGeneratedLines(Timesheet timesheet) {
    List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
    if (CollectionUtils.isEmpty(timesheetLineList)) {
      return;
    }
    timesheet.setTimesheetLineList(
        timesheetLineList.stream()
            .filter(line -> !line.getIsAutomaticallyGenerated())
            .collect(Collectors.toList()));
  }

  protected void createTimesheetLines(
      Timesheet timesheet,
      LocalDate fromDate,
      LocalDate toDate,
      WeeklyPlanning weeklyPlanning,
      EventsPlanning holidayPlanning,
      Employee employee)
      throws AxelorException {
    for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
      createTimesheetLine(timesheet, weeklyPlanning, date, holidayPlanning, employee);
    }
  }

  protected void createTimesheetLine(
      Timesheet timesheet,
      WeeklyPlanning weeklyPlanning,
      LocalDate date,
      EventsPlanning holidayPlanning,
      Employee employee)
      throws AxelorException {
    BigDecimal dayValueInHours =
        weeklyPlanningService.getWorkingDayValueInHours(
            weeklyPlanning, date, LocalTime.MIN, LocalTime.MAX);
    if (dayValueInHours.signum() == 0) {
      return;
    }

    createHolidayTimesheetLine(timesheet, date, holidayPlanning, employee, dayValueInHours);
    createLeaveTimesheetLine(timesheet, employee, date, dayValueInHours);
  }

  protected void createHolidayTimesheetLine(
      Timesheet timesheet,
      LocalDate date,
      EventsPlanning holidayPlanning,
      Employee employee,
      BigDecimal dayValueInHours)
      throws AxelorException {
    AppTimesheet appTimesheet = appHumanResourceService.getAppTimesheet();
    if (!appTimesheet.getCreateLinesForHolidays()
        || !publicHolidayService.checkPublicHolidayDay(date, holidayPlanning)) {
      return;
    }
    TimesheetLine timesheetLine =
        timesheetLineCreateService.createTimesheetLine(
            employee,
            date,
            timesheet,
            dayValueInHours,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_HOLIDAY));
    timesheetLine.setIsAutomaticallyGenerated(true);
  }

  protected void createLeaveTimesheetLine(
      Timesheet timesheet, Employee employee, LocalDate date, BigDecimal dayValueInHours)
      throws AxelorException {
    AppTimesheet appTimesheet = appHumanResourceService.getAppTimesheet();
    if (!appTimesheet.getCreateLinesForLeaves()) {
      return;
    }

    List<LeaveRequest> leaveList = leaveRequestService.getLeaves(employee, date);

    if (ObjectUtils.notEmpty(leaveList)) {
      BigDecimal totalLeaveHours =
          leaveRequestComputeLeaveHoursService.computeTotalLeaveHours(
              date, dayValueInHours, leaveList);
      TimesheetLine timesheetLine =
          timesheetLineCreateService.createTimesheetLine(
              employee,
              date,
              timesheet,
              totalLeaveHours,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAY_LEAVE));
      timesheetLine.setIsAutomaticallyGenerated(true);
    }
  }
}
