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
package com.axelor.apps.businessproject.service.batch;

import com.axelor.apps.base.db.AppBusinessProject;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
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
    Map<String, Object> contextValues = null;
    try {
      contextValues = ProjectInvoicingAssistantBatchService.createJsonContext(batch);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    this.updateTasks(contextValues);
    this.updateTimesheetLines(contextValues);
  }

  private void updateTasks(Map<String, Object> contextValues) {
    AppBusinessProject appBusinessProject = appBusinessProjectService.getAppBusinessProject();
    List<Object> updatedTaskList = new ArrayList<Object>();

    String filter =
        !Strings.isNullOrEmpty(appBusinessProject.getExculdeTaskInvoicing())
            ? "self.id NOT IN (SELECT id FROM ProjectTask WHERE "
                + appBusinessProject.getExculdeTaskInvoicing()
                + ")"
            : "self.id NOT IN (0)";

    Query<ProjectTask> taskQuery =
        projectTaskRepo
            .all()
            .filter(
                filter
                    + " AND self.project.isBusinessProject = :isBusinessProject "
                    + " AND self.project.toInvoice = :invoiceable "
                    + "AND self.toInvoice = :toInvoice")
            .bind("isBusinessProject", true)
            .bind("invoiceable", true)
            .bind("toInvoice", false)
            .order("id");

    int offset = 0;
    List<ProjectTask> taskList;

    while (!(taskList = taskQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();
      offset += taskList.size();
      for (ProjectTask projectTask : taskList) {
        try {
          projectTask =
              projectTaskBusinessProjectService.updateTask(projectTask, appBusinessProject);

          if (projectTask.getToInvoice()) {
            offset--;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", projectTask.getId());
            updatedTaskList.add(map);
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(IExceptionMessage.BATCH_TASK_UPDATION_1), projectTask.getId()),
                  e),
              ExceptionOriginRepository.INVOICE_ORIGIN,
              batch.getId());
        }
      }
      JPA.clear();
    }
    findBatch();
    ProjectInvoicingAssistantBatchService.updateJsonObject(
        batch, updatedTaskList, "updatedTaskSet", contextValues);
  }

  private void updateTimesheetLines(Map<String, Object> contextValues) {

    List<Object> updatedTimesheetLineList = new ArrayList<Object>();

    Query<TimesheetLine> timesheetLineQuery =
        timesheetLineRepo
            .all()
            .filter(
                "((self.projectTask.parentTask.invoicingType = :_invoicingType "
                    + "AND self.projectTask.parentTask.toInvoice = :_teamTaskToInvoice) "
                    + " OR (self.projectTask.parentTask IS NULL "
                    + "AND self.projectTask.invoicingType = :_invoicingType "
                    + "AND self.projectTask.toInvoice = :_projectTaskToInvoice)) "
                    + "AND self.projectTask.project.isBusinessProject = :_isBusinessProject "
                    + "AND self.toInvoice = :_toInvoice")
            .bind("_invoicingType", ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT)
            .bind("_isBusinessProject", true)
            .bind("_projectTaskToInvoice", true)
            .bind("_toInvoice", false)
            .order("id");

    int offset = 0;
    List<TimesheetLine> timesheetLineList;

    while (!(timesheetLineList = timesheetLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();
      offset += timesheetLineList.size();
      for (TimesheetLine timesheetLine : timesheetLineList) {
        try {
          timesheetLine = timesheetLineBusinessService.updateTimesheetLines(timesheetLine);

          if (timesheetLine.getToInvoice()) {
            offset--;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", timesheetLine.getId());
            updatedTimesheetLineList.add(map);
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(IExceptionMessage.BATCH_TIMESHEETLINE_UPDATION_1),
                      timesheetLine.getId()),
                  e),
              ExceptionOriginRepository.INVOICE_ORIGIN,
              batch.getId());
        }
      }
      JPA.clear();
    }
    findBatch();
    ProjectInvoicingAssistantBatchService.updateJsonObject(
        batch, updatedTimesheetLineList, "updatedTimesheetLineSet", contextValues);
  }

  @Override
  protected void stop() {
    String comment = I18n.get(IExceptionMessage.BATCH_TASK_UPDATION_2);

    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
