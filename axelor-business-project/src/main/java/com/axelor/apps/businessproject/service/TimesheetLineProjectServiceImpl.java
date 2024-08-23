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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.timesheet.TimesheetCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.utils.helpers.QueryBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Optional;

public class TimesheetLineProjectServiceImpl extends TimesheetLineServiceImpl
    implements TimesheetLineBusinessService {

  protected ProjectRepository projectRepo;
  protected ProjectTaskRepository projectTaskRepo;
  protected TimesheetLineRepository timesheetLineRepo;
  protected TimesheetCreateService timesheetCreateService;

  @Inject
  public TimesheetLineProjectServiceImpl(
      TimesheetService timesheetService,
      TimesheetRepository timesheetRepo,
      EmployeeRepository employeeRepository,
      ProjectRepository projectRepo,
      ProjectTaskRepository projectTaskaRepo,
      TimesheetLineRepository timesheetLineRepo,
      AppHumanResourceService appHumanResourceService,
      UserHrService userHrService,
      DateService dateService,
      TimesheetCreateService timesheetCreateService) {
    super(
        timesheetService,
        employeeRepository,
        timesheetRepo,
        appHumanResourceService,
        userHrService,
        dateService);

    this.projectRepo = projectRepo;
    this.projectTaskRepo = projectTaskaRepo;
    this.timesheetLineRepo = timesheetLineRepo;
    this.timesheetRepo = timesheetRepo;
    this.timesheetCreateService = timesheetCreateService;
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
      toInvoice =
          projectTask.getToInvoice()
              && projectTask.getInvoicingType() == ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT;
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

  @Override
  public QueryBuilder<TimesheetLine> getTimesheetLineInvoicingFilter() {
    QueryBuilder<TimesheetLine> timespentQueryBuilder =
        QueryBuilder.of(TimesheetLine.class)
            .add("self.projectTask.invoicingType = :_invoicingType")
            .add("self.projectTask.toInvoice = :_projectTaskToInvoice")
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

  @Override
  public Product getDefaultProduct(TimesheetLine timesheetLine) {
    return Optional.ofNullable(timesheetLine)
        .map(TimesheetLine::getProjectTask)
        .map(ProjectTask::getProduct)
        .orElse(super.getDefaultProduct(timesheetLine));
  }
}
