/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineCreateServiceImpl implements TimesheetLineCreateService {
  protected TimesheetLineService timesheetLineService;
  protected TimesheetLineRepository timesheetLineRepository;
  protected UserHrService userHrService;
  protected TimesheetLineCheckService timesheetLineCheckService;

  @Inject
  public TimesheetLineCreateServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineRepository timesheetLineRepository,
      UserHrService userHrService,
      TimesheetLineCheckService timesheetLineCheckService) {
    this.timesheetLineService = timesheetLineService;
    this.timesheetLineRepository = timesheetLineRepository;
    this.userHrService = userHrService;
    this.timesheetLineCheckService = timesheetLineCheckService;
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
      boolean toInvoice)
      throws AxelorException {
    checkDate(date, timesheet);
    Employee employee = AuthUtils.getUser().getEmployee();
    if (employee == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE);
    }
    if (product == null) {
      product = userHrService.getTimesheetProduct(employee);
    }
    timesheetLineCheckService.checkActivity(project, product);
    TimesheetLine timesheetLine =
        timesheetLineService.createTimesheetLine(
            project, product, employee, date, timesheet, duration, comments);
    timesheetLine.setProjectTask(projectTask);
    timesheetLine.setToInvoice(toInvoice);
    return timesheetLineRepository.save(timesheetLine);
  }

  protected void checkDate(LocalDate date, Timesheet timesheet) throws AxelorException {
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();
    if (date != null
        && ((fromDate != null && date.isBefore(fromDate))
            || (toDate != null && date.isAfter(toDate)))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_INVALID_DATE));
    }
  }
}
