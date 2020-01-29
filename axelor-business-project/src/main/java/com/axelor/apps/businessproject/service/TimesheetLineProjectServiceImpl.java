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
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineProjectServiceImpl extends TimesheetLineServiceImpl
    implements TimesheetLineBusinessService {

  @Inject private ProjectRepository projectRepo;
  @Inject private TeamTaskRepository teamTaskaRepo;
  @Inject private TimesheetLineRepository timesheetLineRepo;

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
        && (project.getIsInvoicingTimesheet()
            || (project.getParentProject() != null
                && project.getParentProject().getIsInvoicingTimesheet())))
      timesheetLine.setToInvoice(true);

    return timesheetLine;
  }

  @Override
  public TimesheetLine getDefaultToInvoice(TimesheetLine timesheetLine) {
    Project project =
        timesheetLine.getProject() != null
            ? projectRepo.find(timesheetLine.getProject().getId())
            : null;
    TeamTask teamTask =
        timesheetLine.getTeamTask() != null
            ? teamTaskaRepo.find(timesheetLine.getTeamTask().getId())
            : null;

    Boolean toInvoice = false;
    if (teamTask != null) {
      toInvoice = teamTask.getInvoicingType() == TeamTaskRepository.INVOICING_TYPE_TIME_SPENT;
    } else if (project != null) {
      toInvoice = project.getIsInvoicingTimesheet();
    }
    timesheetLine.setToInvoice(toInvoice);
    return timesheetLine;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  @Override
  public TimesheetLine updateTimesheetLines(TimesheetLine timesheetLine) {
    timesheetLine = getDefaultToInvoice(timesheetLine);
    return timesheetLineRepo.save(timesheetLine);
  }
}
