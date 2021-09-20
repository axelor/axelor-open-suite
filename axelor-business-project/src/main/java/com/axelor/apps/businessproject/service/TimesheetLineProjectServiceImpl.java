/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.businessproject.module.BusinessProjectModule;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.tool.QueryBuilder;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Alternative
@Priority(BusinessProjectModule.PRIORITY)
public class TimesheetLineProjectServiceImpl extends TimesheetLineServiceImpl
    implements TimesheetLineBusinessService {

  protected ProjectRepository projectRepo;
  protected ProjectTaskRepository projectTaskRepo;
  protected TimesheetLineRepository timesheetLineRepo;
  protected TimesheetRepository timesheetRepo;

  @Inject
  public TimesheetLineProjectServiceImpl(
      TimesheetService timesheetService,
      TimesheetRepository timesheetRepo,
      EmployeeRepository employeeRepository,
      ProjectRepository projectRepo,
      ProjectTaskRepository projectTaskaRepo,
      TimesheetLineRepository timesheetLineRepo) {
    super(timesheetService, employeeRepository);

    this.projectRepo = projectRepo;
    this.projectTaskRepo = projectTaskaRepo;
    this.timesheetLineRepo = timesheetLineRepo;
    this.timesheetRepo = timesheetRepo;
  }

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
    ProjectTask projectTask =
        timesheetLine.getProjectTask() != null
            ? projectTaskRepo.find(timesheetLine.getProjectTask().getId())
            : null;

    boolean toInvoice;

    if (projectTask == null && project != null) {
      toInvoice = project.getIsInvoicingTimesheet();
    } else if (projectTask != null) {
      toInvoice = projectTask.getToInvoice();
      if (projectTask.getParentTask() != null) {
        toInvoice =
            projectTask.getParentTask().getInvoicingType()
                == ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT;
      }
    } else {
      toInvoice = false;
    }

    timesheetLine.setToInvoice(toInvoice);
    return timesheetLine;
  }

  @Transactional
  @Override
  public TimesheetLine updateTimesheetLines(TimesheetLine timesheetLine) {
    timesheetLine = getDefaultToInvoice(timesheetLine);
    return timesheetLineRepo.save(timesheetLine);
  }

  @Transactional
  public TimesheetLine setTimesheet(TimesheetLine timesheetLine) {
    Timesheet timesheet =
        timesheetRepo
            .all()
            .filter(
                "self.user = ?1 AND self.company = ?2 AND (self.statusSelect = 1 OR self.statusSelect = 2) AND ((?3 BETWEEN self.fromDate AND self.toDate) OR (self.toDate = null))",
                timesheetLine.getUser(),
                timesheetLine.getProject().getCompany(),
                timesheetLine.getDate())
            .order("id")
            .fetchOne();
    if (timesheet == null) {
      Timesheet lastTimesheet =
          timesheetRepo
              .all()
              .filter(
                  "self.user = ?1 AND self.statusSelect != ?2 AND self.toDate is not null",
                  timesheetLine.getUser(),
                  TimesheetRepository.STATUS_CANCELED)
              .order("-toDate")
              .fetchOne();
      timesheet =
          timesheetService.createTimesheet(
              timesheetLine.getUser(),
              lastTimesheet != null && lastTimesheet.getToDate() != null
                  ? lastTimesheet.getToDate().plusDays(1)
                  : timesheetLine.getDate(),
              null);
      timesheet = timesheetRepo.save(timesheet);
    }
    timesheetLine.setTimesheet(timesheet);
    return timesheetLine;
  }

  @Override
  public QueryBuilder<TimesheetLine> getTimesheetLineInvoicingFilter() {
    QueryBuilder<TimesheetLine> timespentQueryBuilder =
        QueryBuilder.of(TimesheetLine.class)
            .add(
                "((self.projectTask.parentTask.invoicingType = :_invoicingType "
                    + "AND self.projectTask.parentTask.toInvoice = :_teamTaskToInvoice) "
                    + " OR (self.projectTask.parentTask IS NULL "
                    + "AND self.projectTask.invoicingType = :_invoicingType "
                    + "AND self.projectTask.toInvoice = :_projectTaskToInvoice))")
            .add("self.projectTask.project.isBusinessProject = :_isBusinessProject")
            .add("self.toInvoice = :_toInvoice")
            .bind("_invoicingType", ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT)
            .bind("_isBusinessProject", true)
            .bind("_projectTaskToInvoice", true)
            .bind("_toInvoice", false);

    return timespentQueryBuilder;
  }

  @Override
  public void timsheetLineInvoicing(Project project) {
    QueryBuilder<TimesheetLine> timesheetLineQueryBuilder = getTimesheetLineInvoicingFilter();
    timesheetLineQueryBuilder =
        timesheetLineQueryBuilder
            .add("self.project.id = :projectId")
            .bind("projectId", project.getId());

    Query<TimesheetLine> timesheetLineQuery = timesheetLineQueryBuilder.build().order("id");

    int offset = 0;
    List<TimesheetLine> timesheetLineList;

    while (!(timesheetLineList = timesheetLineQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      offset += timesheetLineList.size();
      for (TimesheetLine timesheetLine : timesheetLineList) {
        updateTimesheetLines(timesheetLine);
      }
      JPA.clear();
    }
  }
}
