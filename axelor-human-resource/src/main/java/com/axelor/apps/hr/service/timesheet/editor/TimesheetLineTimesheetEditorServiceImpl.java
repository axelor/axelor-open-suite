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
package com.axelor.apps.hr.service.timesheet.editor;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.EventsPlanningLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.rest.dto.TimesheetLineCount;
import com.axelor.apps.hr.rest.dto.TimesheetLineEditorResponse;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeLeaveDaysService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeLeaveHoursService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCheckService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineUpdateService;
import com.axelor.apps.hr.service.timesheet.TimesheetPeriodComputationService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ResponseConstructor;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;

public class TimesheetLineTimesheetEditorServiceImpl
    implements TimesheetLineTimesheetEditorService {

  protected TimesheetLineService timesheetLineService;
  protected TimesheetLineCheckService timesheetLineCheckService;
  protected TimesheetLineCreateService timesheetLineCreateService;
  protected TimesheetLineUpdateService timesheetLineUpdateService;
  protected TimesheetLineRemoveService timesheetLineRemoveService;
  protected TimesheetPeriodComputationService timesheetPeriodComputationService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected PublicHolidayService publicHolidayService;
  protected LeaveRequestService leaveRequestService;
  protected LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService;
  protected LeaveRequestComputeLeaveHoursService leaveRequestComputeLeaveHoursService;

  @Inject
  public TimesheetLineTimesheetEditorServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineCheckService timesheetLineCheckService,
      TimesheetLineCreateService timesheetLineCreateService,
      TimesheetLineUpdateService timesheetLineUpdateService,
      TimesheetLineRemoveService timesheetLineRemoveService,
      TimesheetPeriodComputationService timesheetPeriodComputationService,
      WeeklyPlanningService weeklyPlanningService,
      LeaveRequestService leaveRequestService,
      PublicHolidayService publicHolidayService,
      LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService,
      LeaveRequestComputeLeaveHoursService leaveRequestComputeLeaveHoursService) {
    this.timesheetLineService = timesheetLineService;
    this.timesheetLineCheckService = timesheetLineCheckService;
    this.timesheetLineCreateService = timesheetLineCreateService;
    this.timesheetLineUpdateService = timesheetLineUpdateService;
    this.timesheetLineRemoveService = timesheetLineRemoveService;
    this.timesheetPeriodComputationService = timesheetPeriodComputationService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.leaveRequestService = leaveRequestService;
    this.publicHolidayService = publicHolidayService;
    this.leaveRequestComputeLeaveDaysService = leaveRequestComputeLeaveDaysService;
    this.leaveRequestComputeLeaveHoursService = leaveRequestComputeLeaveHoursService;
  }

  @Override
  public Response createOrUpdateTimesheetLine(
      Timesheet timesheet,
      Project project,
      ProjectTask projectTask,
      Product product,
      BigDecimal duration,
      BigDecimal hoursDuration,
      LocalDate date,
      String comments,
      Boolean toInvoice)
      throws AxelorException {
    timesheetLineCheckService.checkActivity(project, product);
    TimesheetLine timesheetLine;

    List<TimesheetLine> timesheetLineList =
        timesheetLineService.getTimesheetLines(timesheet, date, project, projectTask);

    if (timesheetLineList.isEmpty()) {
      timesheetLine =
          timesheetLineCreateService.createTimesheetLine(
              project,
              projectTask,
              product,
              date,
              timesheet,
              duration,
              hoursDuration,
              comments,
              toInvoice);

      timesheetPeriodComputationService.setComputedPeriodTotal(timesheet);
    } else {
      timesheetLine = timesheetLineList.get(0);
      if (timesheetLineList.size() == 1) {
        timesheetLineUpdateService.updateTimesheetLine(
            timesheetLine,
            project,
            projectTask,
            product,
            duration,
            hoursDuration,
            date,
            comments,
            toInvoice);

        timesheetPeriodComputationService.setComputedPeriodTotal(timesheet);
      } else {
        Stream<TimesheetLine> timesheetLineStream = timesheetLineList.stream();

        if (hoursDuration != null) {
          hoursDuration =
              hoursDuration.subtract(
                  timesheetLineStream
                      .map(TimesheetLine::getHoursDuration)
                      .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        if (duration != null) {
          duration =
              duration.subtract(
                  timesheetLineStream
                      .map(TimesheetLine::getDuration)
                      .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        timesheetLine =
            timesheetLineCreateService.createTimesheetLine(
                project,
                projectTask,
                timesheetLine.getProduct(),
                date,
                timesheet,
                duration,
                hoursDuration,
                timesheetLine.getComments(),
                timesheetLine.getToInvoice());

        timesheetPeriodComputationService.setComputedPeriodTotal(timesheet);
      }
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.TIMESHEET_LINE_UPDATED),
        buildEditorReponse(timesheet, date, project, projectTask));
  }

  @Override
  public TimesheetLineEditorResponse buildEditorReponse(
      Timesheet timesheet, LocalDate date, Project project, ProjectTask projectTask) {
    List<TimesheetLine> timesheetLineList =
        timesheetLineService.getTimesheetLines(timesheet, date, project, projectTask);

    BigDecimal hoursDuration =
        timesheetLineList.stream()
            .map(TimesheetLine::getHoursDuration)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal duration =
        timesheetLineList.stream()
            .map(TimesheetLine::getDuration)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new TimesheetLineEditorResponse(duration, hoursDuration);
  }

  @Override
  public void removeTimesheetLines(
      Timesheet timesheet, LocalDate date, Project project, ProjectTask projectTask) {
    timesheetLineRemoveService.removeTimesheetLines(
        timesheetLineService.getTimesheetLines(timesheet, date, project, projectTask).stream()
            .map(t -> t.getId().intValue())
            .collect(Collectors.toList()));

    timesheetPeriodComputationService.setComputedPeriodTotal(timesheet);
  }

  @Override
  public Response getTimesheetLineCount(Timesheet timesheet) {
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();

    if (toDate == null) {
      toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
    }

    Map<LocalDate, TimesheetLineCount> dateTSLDurationSummaryMap = new HashMap<>();

    if (toDate != null) {
      Employee employee = timesheet.getEmployee();
      List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();

      fromDate
          .datesUntil(toDate.plusDays(1))
          .forEach(
              it -> {
                try {
                  updateCount(dateTSLDurationSummaryMap, it, employee, timesheetLineList);
                } catch (AxelorException e) {
                  throw new RuntimeException(e);
                }
              });
    }

    return ResponseConstructor.build(Response.Status.OK, dateTSLDurationSummaryMap);
  }

  protected void updateCount(
      Map<LocalDate, TimesheetLineCount> map,
      LocalDate date,
      Employee employee,
      List<TimesheetLine> timeSheetLineList)
      throws AxelorException {

    String leaveReason = null;
    BigDecimal weeklyPlanningDuration = BigDecimal.ZERO;
    BigDecimal weeklyPlanningHoursDuration = BigDecimal.ZERO;
    BigDecimal leaveDuration = BigDecimal.ZERO;
    BigDecimal leaveHoursDuration = BigDecimal.ZERO;

    EventsPlanning holidayPlanning = employee.getPublicHolidayEventsPlanning();
    if (publicHolidayService.checkPublicHolidayDay(date, holidayPlanning)) {
      List<EventsPlanningLine> eventsPlanningList =
          publicHolidayService.getPublicHolidayList(date, holidayPlanning);
      leaveReason =
          eventsPlanningList.stream().map(x -> x.getDescription()).collect(Collectors.joining(","));
    } else {
      WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();

      weeklyPlanningDuration =
          new BigDecimal(weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, date));
      weeklyPlanningHoursDuration =
          weeklyPlanningService.getWorkingDayValueInHours(
              weeklyPlanning, date, LocalTime.MIN, LocalTime.MAX);

      if (leaveRequestService.isLeaveDay(employee, date)) {
        List<LeaveRequest> leaveRequestList = leaveRequestService.getLeaves(employee, date);

        leaveDuration = this.computeTotalLeaveDays(employee, date, leaveRequestList);

        leaveHoursDuration =
            leaveRequestComputeLeaveHoursService.computeTotalLeaveHours(
                date, weeklyPlanningHoursDuration, leaveRequestList);
        leaveReason =
            leaveRequestList.stream()
                .map(x -> x.getLeaveReason().getName())
                .collect(Collectors.joining(","));
      }
    }

    BigDecimal duration =
        timeSheetLineList.stream()
            .filter(x -> x.getDate().equals(date))
            .map(TimesheetLine::getDuration)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal hoursDuration =
        timeSheetLineList.stream()
            .filter(x -> x.getDate().equals(date))
            .map(TimesheetLine::getHoursDuration)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    map.put(
        date,
        new TimesheetLineCount(
            duration,
            hoursDuration,
            weeklyPlanningDuration,
            weeklyPlanningHoursDuration,
            leaveDuration,
            leaveHoursDuration,
            leaveReason));
  }

  private BigDecimal computeTotalLeaveDays(
      Employee employee, LocalDate date, List<LeaveRequest> leaveRequestList) {
    BigDecimal leaveDayCount = BigDecimal.ZERO;

    if (ObjectUtils.notEmpty(leaveRequestList)) {
      for (LeaveRequest leaveRequest : leaveRequestList) {
        leaveDayCount =
            leaveDayCount.add(
                leaveRequestComputeLeaveDaysService.computeLeaveDaysByLeaveRequest(
                    date, date, leaveRequest, employee));
      }
    }
    return leaveDayCount.setScale(2, RoundingMode.HALF_UP);
  }
}
