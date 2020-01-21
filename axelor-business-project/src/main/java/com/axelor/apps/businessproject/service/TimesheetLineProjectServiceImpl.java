/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineProjectServiceImpl extends TimesheetLineServiceImpl {

  @Override
  public TimesheetLine createTimesheetLine(
      Project project,
      Product product,
      User user,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal hours,
      String comments) {
    TimesheetLine timesheetLine =
        super.createTimesheetLine(project, product, user, date, timesheet, hours, comments);

    if (Beans.get(AppBusinessProjectService.class).isApp("business-project")
        && project != null
        && (project.getProjInvTypeSelect() == ProjectRepository.INVOICING_TYPE_TIME_BASED
            || (project.getParentProject() != null
                && project.getParentProject().getProjInvTypeSelect()
                    == ProjectRepository.INVOICING_TYPE_TIME_BASED)))
      timesheetLine.setToInvoice(true);

    return timesheetLine;
  }
}
