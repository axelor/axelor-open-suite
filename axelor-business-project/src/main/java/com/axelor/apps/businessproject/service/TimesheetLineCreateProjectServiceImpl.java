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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCheckService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineCreateProjectServiceImpl extends TimesheetLineCreateServiceImpl {
  @Inject
  public TimesheetLineCreateProjectServiceImpl(
      TimesheetLineService timesheetLineService,
      TimesheetLineRepository timesheetLineRepository,
      UserHrService userHrService,
      TimesheetLineCheckService timesheetLineCheckService,
      AppBaseService appBaseService,
      ProjectPlanningTimeRepository projectPlanningTimeRepository) {
    super(
        timesheetLineService,
        timesheetLineRepository,
        userHrService,
        timesheetLineCheckService,
        appBaseService,
        projectPlanningTimeRepository);
  }

  @Override
  public TimesheetLine createTimesheetLine(
      Project project,
      Product product,
      Employee employee,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal hours,
      String comments) {
    TimesheetLine timesheetLine =
        super.createTimesheetLine(project, product, employee, date, timesheet, hours, comments);

    if (Beans.get(AppBusinessProjectService.class).isApp("business-project")
        && project != null
        && (project.getIsInvoicingTimesheet()
            || (project.getParentProject() != null
                && project.getParentProject().getIsInvoicingTimesheet())))
      timesheetLine.setToInvoice(true);

    return timesheetLine;
  }
}
