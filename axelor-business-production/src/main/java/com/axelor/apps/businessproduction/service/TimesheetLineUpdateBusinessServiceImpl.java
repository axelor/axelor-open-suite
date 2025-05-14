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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCheckService;
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
      TimesheetLineCheckService timesheetLineCheckService,
      TimesheetLineBusinessService timesheetLineBusinessService,
      AppProductionService appProductionService) {
    super(timesheetLineService, timesheetLineCheckService);
    this.timesheetLineBusinessService = timesheetLineBusinessService;
    this.appProductionService = appProductionService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updateTimesheetLine(
      TimesheetLine timesheetLine,
      Project project,
      ProjectTask projectTask,
      Product product,
      BigDecimal duration,
      BigDecimal hoursDuration,
      LocalDate date,
      String comments,
      Boolean toInvoice)
      throws AxelorException {
    super.updateTimesheetLine(
        timesheetLine,
        project,
        projectTask,
        product,
        duration,
        hoursDuration,
        date,
        comments,
        toInvoice);
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
      Product product,
      BigDecimal duration,
      BigDecimal hoursDuration,
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
        timesheetLine,
        project,
        projectTask,
        product,
        duration,
        hoursDuration,
        date,
        comments,
        toInvoice);

    if (manufOrder != null) {
      timesheetLine.setManufOrder(manufOrder);
    }
    if (operationOrder != null) {
      timesheetLine.setOperationOrder(operationOrder);
    }
  }
}
