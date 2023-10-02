/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBusinessProject;
import com.axelor.utils.QueryBuilder;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchUpdateTaskService extends AbstractBatch {

  protected AppBusinessProjectService appBusinessProjectService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  protected TimesheetLineBusinessService timesheetLineBusinessService;
  protected ProjectTaskRepository projectTaskRepo;
  protected TimesheetLineRepository timesheetLineRepo;

  @Inject
  public BatchUpdateTaskService(
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      AppBusinessProjectService appBusinessProjectService,
      TimesheetLineBusinessService timesheetLineBusinessService,
      ProjectTaskRepository projectTaskRepo,
      TimesheetLineRepository timesheetLineRepo) {
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.timesheetLineBusinessService = timesheetLineBusinessService;
    this.projectTaskRepo = projectTaskRepo;
    this.timesheetLineRepo = timesheetLineRepo;
  }

  @Override
  protected void process() {

    this.updateTasks();

    Map<String, Object> contextValues = null;
    try {
      contextValues = BusinessProjectBatchService.createJsonContext(batch);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    AppBusinessProject appBusinessProject = appBusinessProjectService.getAppBusinessProject();
    if (!appBusinessProject.getAutomaticInvoicing()) {
      this.updateTaskToInvoice(contextValues, appBusinessProject);
      this.updateTimesheetLines(contextValues);
    }
  }

  protected void updateTasks() {
    QueryBuilder<ProjectTask> taskQueryBuilder =
        projectTaskBusinessProjectService.getTaskInvoicingFilter();

    Query<ProjectTask> taskQuery = taskQueryBuilder.build().order("id");

    int offset = 0;
    List<ProjectTask> taskList;

    while (!(taskList = taskQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();
      for (ProjectTask projectTask : taskList) {
        try {
          projectTaskBusinessProjectService.setProjectTaskValues(projectTask);
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(
              e,
              String.format(
                  I18n.get(BusinessProjectExceptionMessage.BATCH_TASK_UPDATION_1),
                  projectTask.getId()),
              batch.getId());
        }
      }
      offset += taskList.size();
      JPA.clear();
    }
  }

  protected void updateTaskToInvoice(
      Map<String, Object> contextValues, AppBusinessProject appBusinessProject) {

    QueryBuilder<ProjectTask> taskQueryBuilder =
        projectTaskBusinessProjectService.getTaskInvoicingFilter();

    if (!Strings.isNullOrEmpty(appBusinessProject.getExcludeTaskInvoicing())) {
      String filter = "NOT (" + appBusinessProject.getExcludeTaskInvoicing() + ")";
      taskQueryBuilder = taskQueryBuilder.add(filter);
    }
    Query<ProjectTask> taskQuery = taskQueryBuilder.build().order("id");

    int offset = 0;
    List<ProjectTask> taskList;
    List<Object> updatedTaskList = new ArrayList<Object>();

    while (!(taskList = taskQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();
      offset += taskList.size();
      for (ProjectTask projectTask : taskList) {
        try {
          projectTask =
              projectTaskBusinessProjectService.updateTaskToInvoice(
                  projectTask, appBusinessProject);

          if (projectTask.getToInvoice()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", projectTask.getId());
            updatedTaskList.add(map);
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(BusinessProjectExceptionMessage.BATCH_TASK_UPDATION_1),
                      projectTask.getId()),
                  e),
              ExceptionOriginRepository.INVOICE_ORIGIN,
              batch.getId());
        }
      }
      JPA.clear();
    }
    findBatch();
    BusinessProjectBatchService.updateJsonObject(
        batch, updatedTaskList, "updatedTaskSet", contextValues);
  }

  protected void updateTimesheetLines(Map<String, Object> contextValues) {

    List<Object> updatedTimesheetLineList = new ArrayList<Object>();

    QueryBuilder<TimesheetLine> timesheetLineQueryBuilder =
        timesheetLineBusinessService.getTimesheetLineInvoicingFilter();
    Query<TimesheetLine> timesheetLineQuery = timesheetLineQueryBuilder.build().order("id");

    int offset = 0;
    List<TimesheetLine> timesheetLineList;

    while (!(timesheetLineList = timesheetLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();
      offset += timesheetLineList.size();
      for (TimesheetLine timesheetLine : timesheetLineList) {
        try {
          timesheetLine = timesheetLineBusinessService.updateTimesheetLines(timesheetLine);
          if (timesheetLine.getToInvoice()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", timesheetLine.getId());
            updatedTimesheetLineList.add(map);
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(BusinessProjectExceptionMessage.BATCH_TIMESHEETLINE_UPDATION_1),
                      timesheetLine.getId()),
                  e),
              ExceptionOriginRepository.INVOICE_ORIGIN,
              batch.getId());
        }
      }
      JPA.clear();
    }
    findBatch();
    BusinessProjectBatchService.updateJsonObject(
        batch, updatedTimesheetLineList, "updatedTimesheetLineSet", contextValues);
  }

  @Override
  protected void stop() {
    String comment = I18n.get(BusinessProjectExceptionMessage.BATCH_TASK_UPDATION_2);

    comment +=
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BUSINESS_PROJECT_BATCH);
  }
}
