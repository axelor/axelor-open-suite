package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineCreateBusinessServiceImpl implements TimesheetLineCreateBusinessService {
  protected AppProductionService appProductionService;
  protected TimesheetLineCreateService timesheetLineCreateService;

  @Inject
  public TimesheetLineCreateBusinessServiceImpl(
      AppProductionService appProductionService,
      TimesheetLineCreateService timesheetLineCreateService) {
    this.appProductionService = appProductionService;
    this.timesheetLineCreateService = timesheetLineCreateService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public TimesheetLine createTimesheetLine(
      Project project,
      ProjectTask projectTask,
      Product product,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal duration,
      String comments,
      boolean toInvoice,
      ManufOrder manufOrder,
      ManufacturingOperation manufacturingOperation)
      throws AxelorException {
    boolean timesheetEnabledOnManufOrder =
        appProductionService.getAppProduction().getEnableTimesheetOnManufOrder();
    if ((manufOrder != null && !timesheetEnabledOnManufOrder)
        || (manufacturingOperation != null && !timesheetEnabledOnManufOrder)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BusinessProductionExceptionMessage.TIMESHEET_MANUF_ORDER_NOT_ENABLED));
    }

    TimesheetLine timesheetLine =
        timesheetLineCreateService.createTimesheetLine(
            project, projectTask, product, date, timesheet, duration, comments, toInvoice);

    timesheetLine.setManufOrder(manufOrder);
    timesheetLine.setManufacturingOperation(manufacturingOperation);
    timesheetLine.setToInvoice(toInvoice);
    return timesheetLine;
  }
}
