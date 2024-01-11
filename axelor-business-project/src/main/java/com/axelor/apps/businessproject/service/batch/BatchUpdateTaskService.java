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
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBusinessProject;
import com.axelor.utils.QueryBuilder;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
      contextValues = ProjectInvoicingAssistantBatchService.createJsonContext(batch);
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
    List<Long> projectTaskIdList =
        projectTaskBusinessProjectService.getTaskInvoicingFilter().build().order("id").select("id")
            .fetch(0, 0).stream()
            .map(m -> (Long) m.get("id"))
            .collect(Collectors.toList());

    int offset = 0;
    findBatch();

    for (Long projectTaskId : projectTaskIdList) {
      offset++;
      try {
        projectTaskBusinessProjectService.setProjectTaskValues(projectTaskRepo.find(projectTaskId));
        if (appBusinessProjectService.getAppBusinessProject().getAutomaticInvoicing()) {
          incrementDone();
        }
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            e,
            String.format(
                I18n.get(BusinessProjectExceptionMessage.BATCH_TASK_UPDATION_1), projectTaskId),
            batch.getId());
      }
      if (offset % FETCH_LIMIT == 0) {
        JPA.clear();
        findBatch();
      }
    }
  }

  protected void updateTaskToInvoice(
      Map<String, Object> contextValues, AppBusinessProject appBusinessProject) {
    List<Object> updatedTaskList = new ArrayList<>();
    QueryBuilder<ProjectTask> projectTaskQueryBuilder =
        projectTaskBusinessProjectService.getTaskInvoicingFilter();
    if (!Strings.isNullOrEmpty(appBusinessProject.getExcludeTaskInvoicing())) {
      String filter = "NOT (" + appBusinessProject.getExcludeTaskInvoicing() + ")";
      projectTaskQueryBuilder = projectTaskQueryBuilder.add(filter);
    }
    List<Long> projectTaskIdList =
        projectTaskQueryBuilder.build().order("id").select("id").fetch(0, 0).stream()
            .map(m -> (Long) m.get("id"))
            .collect(Collectors.toList());

    int offset = 0;
    ProjectTask projectTask;
    findBatch();

    for (Long projectTaskId : projectTaskIdList) {
      offset++;
      try {
        projectTask =
            projectTaskBusinessProjectService.updateTaskToInvoice(
                projectTaskRepo.find(projectTaskId), appBusinessProject);
        if (projectTask.getToInvoice()) {
          Map<String, Object> map = new HashMap<>();
          map.put("id", projectTask.getId());
          updatedTaskList.add(map);
        }
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            new Exception(
                String.format(
                    I18n.get(BusinessProjectExceptionMessage.BATCH_TASK_UPDATION_1), projectTaskId),
                e),
            ExceptionOriginRepository.INVOICE_ORIGIN,
            batch.getId());
      }
      if (offset % FETCH_LIMIT == 0) {
        JPA.clear();
        findBatch();
      }
      ProjectInvoicingAssistantBatchService.updateJsonObject(
          batch, updatedTaskList, "updatedTaskSet", contextValues);
    }
  }

  protected void updateTimesheetLines(Map<String, Object> contextValues) {
    List<Object> updatedTimesheetLineList = new ArrayList<>();
    List<Long> timesheetLineIdList =
        timesheetLineBusinessService.getTimesheetLineInvoicingFilter().build().order("id")
            .select("id").fetch(0, 0).stream()
            .map(m -> (Long) m.get("id"))
            .collect(Collectors.toList());

    int offset = 0;
    TimesheetLine timesheetLine;
    findBatch();

    for (Long timesheetLineId : timesheetLineIdList) {
      offset++;
      try {
        timesheetLine =
            timesheetLineBusinessService.updateTimesheetLines(
                timesheetLineRepo.find(timesheetLineId));
        if (timesheetLine.getToInvoice()) {
          Map<String, Object> map = new HashMap<>();
          map.put("id", timesheetLine.getId());
          updatedTimesheetLineList.add(map);
        }
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            new Exception(
                String.format(
                    I18n.get(BusinessProjectExceptionMessage.BATCH_TIMESHEETLINE_UPDATION_1),
                    timesheetLineId),
                e),
            ExceptionOriginRepository.INVOICE_ORIGIN,
            batch.getId());
      }
      if (offset % FETCH_LIMIT == 0) {
        JPA.clear();
        findBatch();
      }
    }
    ProjectInvoicingAssistantBatchService.updateJsonObject(
        batch, updatedTimesheetLineList, "updatedTimesheetLineSet", contextValues);
  }

  @Override
  protected void stop() {
    String comment = I18n.get(BusinessProjectExceptionMessage.BATCH_TASK_UPDATION_2);

    comment +=
        String.format(
            "\t" + I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_PROJECT_INVOICING_ASSISTANT_BATCH);
  }
}
