/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.leave.LeaveRequestComputeDurationService;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppTimesheet;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** @author axelor */
public class TimesheetServiceImpl implements TimesheetService {
  protected TimesheetLineService timesheetLineService;
  protected AppHumanResourceService appHumanResourceService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected PublicHolidayService publicHolidayService;
  protected LeaveRequestService leaveRequestService;
  protected LeaveRequestComputeDurationService leaveRequestComputeDurationService;

  @Inject
  public TimesheetServiceImpl(
      TimesheetLineService timesheetLineService,
      AppHumanResourceService appHumanResourceService,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayService publicHolidayService,
      LeaveRequestService leaveRequestService,
      LeaveRequestComputeDurationService leaveRequestComputeDurationService) {
    this.timesheetLineService = timesheetLineService;
    this.appHumanResourceService = appHumanResourceService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayService = publicHolidayService;
    this.leaveRequestService = leaveRequestService;
    this.leaveRequestComputeDurationService = leaveRequestComputeDurationService;
  }

  @Override
  public String getPeriodTotalConvertTitle(Timesheet timesheet) {
    String title = "";
    if (timesheet != null) {
      if (timesheet.getTimeLoggingPreferenceSelect() != null) {
        title = timesheet.getTimeLoggingPreferenceSelect();
      }
    } else {
      title = Beans.get(AppBaseService.class).getAppBase().getTimeLoggingPreferenceSelect();
    }
    switch (title) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        return I18n.get("Days");
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return I18n.get("Minutes");
      default:
        return I18n.get("Hours");
    }
  }

  @Override
  public void updateTimeLoggingPreference(Timesheet timesheet) throws AxelorException {
    String timeLoggingPref;
    if (timesheet.getEmployee() == null) {
      timeLoggingPref = EmployeeRepository.TIME_PREFERENCE_HOURS;
    } else {
      Employee employee = timesheet.getEmployee();
      timeLoggingPref = employee.getTimeLoggingPreferenceSelect();
    }
    timesheet.setTimeLoggingPreferenceSelect(timeLoggingPref);

    if (timesheet.getTimesheetLineList() != null) {
      for (TimesheetLine timesheetLine : timesheet.getTimesheetLineList()) {
        timesheetLine.setDuration(
            timesheetLineService.computeHoursDuration(
                timesheet, timesheetLine.getHoursDuration(), false));
      }
    }
  }

  @Override
  public void prefillLines(Timesheet timesheet) throws AxelorException {

    AppTimesheet appTimesheet = appHumanResourceService.getAppTimesheet();

    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();

    Employee employee = timesheet.getEmployee();
    HRConfig config = timesheet.getCompany().getHrConfig();
    WeeklyPlanning weeklyPlanning =
        employee != null ? employee.getWeeklyPlanning() : config.getWeeklyPlanning();
    EventsPlanning holidayPlanning =
        employee != null
            ? employee.getPublicHolidayEventsPlanning()
            : config.getPublicHolidayEventsPlanning();

    for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
      BigDecimal dayValueInHours =
          weeklyPlanningService.getWorkingDayValueInHours(
              weeklyPlanning, date, LocalTime.MIN, LocalTime.MAX);

      if (appTimesheet.getCreateLinesForHolidays()
          && publicHolidayService.checkPublicHolidayDay(date, holidayPlanning)) {
        timesheetLineService.createTimesheetLine(
            employee,
            date,
            timesheet,
            dayValueInHours,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_HOLIDAY));

      } else if (appTimesheet.getCreateLinesForLeaves()) {
        List<LeaveRequest> leaveList = leaveRequestService.getLeaves(employee, date);
        BigDecimal totalLeaveHours = BigDecimal.ZERO;
        if (ObjectUtils.notEmpty(leaveList)) {
          for (LeaveRequest leave : leaveList) {
            BigDecimal leaveHours =
                leaveRequestComputeDurationService.computeDuration(leave, date, date);
            if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
              leaveHours = leaveHours.multiply(dayValueInHours);
            }
            totalLeaveHours = totalLeaveHours.add(leaveHours);
          }
          timesheetLineService.createTimesheetLine(
              employee,
              date,
              timesheet,
              totalLeaveHours,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAY_LEAVE));
        }
      }
    }
  }
}
