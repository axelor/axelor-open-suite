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
      BigDecimal hoursDuration,
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
    if (hoursDuration != null) {
      timesheetLine.setHoursDuration(hoursDuration);
      timesheetLine.setDuration(
          timesheetLineService.computeHoursDuration(
              timesheetLine.getTimesheet(), hoursDuration, false));
    }
    if (duration != null) {
      timesheetLine.setDuration(duration);
      timesheetLine.setHoursDuration(
          timesheetLineService.computeHoursDuration(timesheetLine.getTimesheet(), duration, true));
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
