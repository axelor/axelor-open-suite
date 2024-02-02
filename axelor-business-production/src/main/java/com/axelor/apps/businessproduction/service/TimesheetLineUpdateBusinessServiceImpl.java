package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineUpdateServiceImpl;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineUpdateBusinessServiceImpl extends TimesheetLineUpdateServiceImpl
    implements TimesheetLineUpdateBusinessService {
  protected TimesheetLineBusinessService timesheetLineBusinessService;
  protected AppProductionService appProductionService;

  @Inject
  public TimesheetLineUpdateBusinessServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineBusinessService timesheetLineBusinessService,
      AppProductionService appProductionService) {
    super(timesheetLineService);
    this.timesheetLineBusinessService = timesheetLineBusinessService;
    this.appProductionService = appProductionService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updateTimesheetLine(
      TimesheetLine timesheetLine,
      Project project,
      ProjectTask projectTask,
      BigDecimal duration,
      LocalDate date,
      String comments,
      Boolean toInvoice)
      throws AxelorException {
    super.updateTimesheetLine(
        timesheetLine, project, projectTask, duration, date, comments, toInvoice);
    if (!appProductionService.isApp("production")) {
      return;
    }
    timesheetLineBusinessService.getDefaultToInvoice(timesheetLine);
    timesheetLine.setToInvoice(toInvoice);
  }

  @Override
  public void updateTimesheetLine(
      TimesheetLine timesheetLine,
      Project project,
      ProjectTask projectTask,
      BigDecimal duration,
      LocalDate date,
      String comments,
      Boolean toInvoice,
      ManufOrder manufOrder,
      OperationOrder operationOrder)
      throws AxelorException {
    boolean timesheetEnabledOnManufOrder =
        appProductionService.getAppProduction().getEnableTimesheetOnManufOrder();
    if ((manufOrder != null && !timesheetEnabledOnManufOrder)
        || (operationOrder != null && !timesheetEnabledOnManufOrder)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BusinessProductionExceptionMessage.TIMESHEET_MANUF_ORDER_NOT_ENABLED));
    }

    this.updateTimesheetLine(
        timesheetLine, project, projectTask, duration, date, comments, toInvoice);

    if (manufOrder != null) {
      timesheetLine.setManufOrder(manufOrder);
    }
    if (operationOrder != null) {
      timesheetLine.setOperationOrder(operationOrder);
    }
  }
}
