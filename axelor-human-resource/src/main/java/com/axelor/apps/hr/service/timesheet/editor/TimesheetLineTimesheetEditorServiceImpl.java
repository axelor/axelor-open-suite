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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.rest.dto.TimesheetLineCount;
import com.axelor.apps.hr.rest.dto.TimesheetLineEditorResponse;
import com.axelor.apps.hr.rest.dto.TimesheetLinePostRequest;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCheckService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineUpdateService;
import com.axelor.apps.hr.service.timesheet.TimesheetPeriodComputationService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  protected AllocationLineComputeService allocationLineComputeService;
  protected AppHumanResourceService appHumanResourceService;

  @Inject
  public TimesheetLineTimesheetEditorServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineCheckService timesheetLineCheckService,
      TimesheetLineCreateService timesheetLineCreateService,
      TimesheetLineUpdateService timesheetLineUpdateService,
      TimesheetLineRemoveService timesheetLineRemoveService,
      TimesheetPeriodComputationService timesheetPeriodComputationService,
      WeeklyPlanningService weeklyPlanningService,
      AllocationLineComputeService allocationLineComputeService,
      AppHumanResourceService appHumanResourceService) {
    this.timesheetLineService = timesheetLineService;
    this.timesheetLineCheckService = timesheetLineCheckService;
    this.timesheetLineCreateService = timesheetLineCreateService;
    this.timesheetLineUpdateService = timesheetLineUpdateService;
    this.timesheetLineRemoveService = timesheetLineRemoveService;
    this.timesheetPeriodComputationService = timesheetPeriodComputationService;
    this.weeklyPlanningService = weeklyPlanningService;
    this.allocationLineComputeService = allocationLineComputeService;
    this.appHumanResourceService = appHumanResourceService;
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
      fromDate
          .datesUntil(toDate)
          .forEach(it -> updateCount(dateTSLDurationSummaryMap, it, timesheet));
    }

    return ResponseConstructor.build(Response.Status.OK, dateTSLDurationSummaryMap);
  }

  protected void updateCount(
      Map<LocalDate, TimesheetLineCount> map, LocalDate date, Timesheet timesheet) {
    List<TimesheetLine> timeSheetLineList = timesheet.getTimesheetLineList();

    Employee employee = timesheet.getEmployee();
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();

    BigDecimal weeklyPlanningDuration =
        new BigDecimal(weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, date));
    BigDecimal weeklyPlanningHoursDuration =
        weeklyPlanningService.getWorkingDayValueInHours(
            weeklyPlanning, date, LocalTime.MIN, LocalTime.MAX);

    BigDecimal leavesDuration;
    try {
      leavesDuration = allocationLineComputeService.getLeaves(date, date, employee);
    } catch (AxelorException e) {
      e.printStackTrace();
      leavesDuration = new BigDecimal(-1);
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
            leavesDuration));
  }

  @Override
  public int updateToInvoice(TimesheetLinePostRequest timesheetLinePostRequest) {
    int count = 0;

    if (appHumanResourceService.getAppTimesheet().getEnableActivity()) {
      Long timesheetId = timesheetLinePostRequest.getTimesheetId();
      LocalDate date = timesheetLinePostRequest.getDate();

      Preconditions.checkNotNull(
          timesheetId,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_EDITOR_TIMESHEET_ID_IS_REQUIRED));
      Preconditions.checkNotNull(
          date, I18n.get(HumanResourceExceptionMessage.TIMESHEET_EDITOR_DATE_IS_REQUIRED));

      Timesheet timesheet =
          ObjectFinder.find(Timesheet.class, timesheetId, ObjectFinder.NO_VERSION);

      Project project =
          Optional.ofNullable(timesheetLinePostRequest.getProjectId())
              .map(
                  x ->
                      ObjectFinder.find(
                          Project.class,
                          timesheetLinePostRequest.getProjectId(),
                          ObjectFinder.NO_VERSION))
              .orElse(null);

      ProjectTask projectTask =
          Optional.ofNullable(timesheetLinePostRequest.getProjectTaskId())
              .map(
                  x ->
                      ObjectFinder.find(
                          ProjectTask.class,
                          timesheetLinePostRequest.getProjectTaskId(),
                          ObjectFinder.NO_VERSION))
              .orElse(null);

      List<TimesheetLine> timesheetLineList =
          timesheetLineService.getTimesheetLines(timesheet, date, project, projectTask);

      boolean toInvoice = timesheetLinePostRequest.isToInvoice();

      for (TimesheetLine timesheetLine : timesheetLineList) {
        if (timesheetLine.getProduct() != null) {
          updateTSLToInvoice(timesheetLine, toInvoice);
          count++;
        }
      }
    }

    return count;
  }

  @Transactional
  protected void updateTSLToInvoice(TimesheetLine timesheetLine, boolean toInvoice) {
    timesheetLine.setToInvoice(toInvoice);
  }
}
