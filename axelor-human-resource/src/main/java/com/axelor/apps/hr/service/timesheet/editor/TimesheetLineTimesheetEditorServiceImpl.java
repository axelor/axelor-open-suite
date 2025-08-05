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
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.rest.dto.TimesheetLineDurationSummary;
import com.axelor.apps.hr.rest.dto.TimesheetLineEditorResponse;
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
import com.axelor.utils.api.ResponseConstructor;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
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

  @Inject
  public TimesheetLineTimesheetEditorServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineCheckService timesheetLineCheckService,
      TimesheetLineCreateService timesheetLineCreateService,
      TimesheetLineUpdateService timesheetLineUpdateService,
      TimesheetLineRemoveService timesheetLineRemoveService,
      TimesheetPeriodComputationService timesheetPeriodComputationService) {
    this.timesheetLineService = timesheetLineService;
    this.timesheetLineCheckService = timesheetLineCheckService;
    this.timesheetLineCreateService = timesheetLineCreateService;
    this.timesheetLineUpdateService = timesheetLineUpdateService;
    this.timesheetLineRemoveService = timesheetLineRemoveService;
    this.timesheetPeriodComputationService = timesheetPeriodComputationService;
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
    List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();

    Map<LocalDate, TimesheetLineDurationSummary> dateTSLDurationSummaryMap =
        timesheetLineList.stream()
            .collect(
                Collectors.groupingBy(
                    TimesheetLine::getDate,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                          BigDecimal duration =
                              list.stream()
                                  .map(TimesheetLine::getDuration)
                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
                          BigDecimal hoursDuration =
                              list.stream()
                                  .map(TimesheetLine::getHoursDuration)
                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
                          return new TimesheetLineDurationSummary(duration, hoursDuration);
                        })));

    return ResponseConstructor.build(Response.Status.OK, dateTSLDurationSummaryMap);
  }
}
