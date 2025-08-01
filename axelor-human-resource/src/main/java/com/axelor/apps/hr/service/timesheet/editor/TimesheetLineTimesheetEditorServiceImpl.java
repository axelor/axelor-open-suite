package com.axelor.apps.hr.service.timesheet.editor;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.rest.dto.TimesheetLineResponse;
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

      return ResponseConstructor.buildCreateResponse(
          timesheetLine, new TimesheetLineResponse(timesheetLine));

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

        return ResponseConstructor.build(
            Response.Status.OK,
            I18n.get(ITranslation.TIMESHEET_LINE_UPDATED),
            new TimesheetLineResponse(timesheetLine));
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

        return ResponseConstructor.buildCreateResponse(
            timesheetLine, new TimesheetLineResponse(timesheetLine));
      }
    }
  }

  @Override
  public void removeAllTimesheetLines(Timesheet timesheet) {
    timesheetLineRemoveService.removeTimesheetLines(
        timesheet.getTimesheetLineList().stream()
            .map(t -> t.getId().intValue())
            .collect(Collectors.toList()));

    timesheetPeriodComputationService.setComputedPeriodTotal(timesheet);
  }
}
