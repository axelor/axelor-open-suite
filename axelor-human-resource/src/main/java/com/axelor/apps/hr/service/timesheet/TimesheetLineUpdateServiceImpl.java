package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineUpdateServiceImpl implements TimesheetLineUpdateService {

  protected TimesheetLineService timesheetLineService;
  protected TimesheetLineCheckService timesheetLineCheckService;

  @Inject
  public TimesheetLineUpdateServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineCheckService timesheetLineCheckService) {
    this.timesheetLineService = timesheetLineService;
    this.timesheetLineCheckService = timesheetLineCheckService;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void updateTimesheetLine(
      TimesheetLine timesheetLine,
      Project project,
      ProjectTask projectTask,
      Product product,
      BigDecimal duration,
      LocalDate date,
      String comments,
      Boolean toInvoice)
      throws AxelorException {
    project = project != null ? project : timesheetLine.getProject();
    product = product != null ? product : timesheetLine.getProduct();
    timesheetLineCheckService.checkActivity(project, product);

    if (project != null) {
      timesheetLine.setProject(project);
    }
    if (projectTask != null) {
      timesheetLine.setProjectTask(projectTask);
    }
    if (product != null) {
      timesheetLine.setProduct(product);
    }
    if (duration != null) {
      timesheetLine.setHoursDuration(duration);
      timesheetLine.setDuration(
          timesheetLineService.computeHoursDuration(timesheetLine.getTimesheet(), duration, false));
    }
    if (date != null) {
      timesheetLine.setDate(date);
    }
    if (StringUtils.notEmpty(comments)) {
      timesheetLine.setComments(comments);
    }
    timesheetLine.setToInvoice(toInvoice);
  }
}
