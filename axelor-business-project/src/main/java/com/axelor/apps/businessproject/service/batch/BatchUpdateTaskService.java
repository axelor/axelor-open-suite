/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.businessproject.service.TeamTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchUpdateTaskService extends AbstractBatch {

  protected AppBusinessProjectService appBusinessProjectService;
  protected TeamTaskBusinessProjectService teamTaskBusinessProjectService;
  protected TimesheetLineBusinessService timesheetLineBusinessService;
  protected TeamTaskRepository teamTaskRepo;
  protected TimesheetLineRepository timesheetLineRepo;

  @Inject
  public BatchUpdateTaskService(
      TeamTaskBusinessProjectService teamTaskBusinessProjectService,
      AppBusinessProjectService appBusinessProjectService,
      TimesheetLineBusinessService timesheetLineBusinessService,
      TeamTaskRepository teamTaskRepo,
      TimesheetLineRepository timesheetLineRepo) {
    this.teamTaskBusinessProjectService = teamTaskBusinessProjectService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.timesheetLineBusinessService = timesheetLineBusinessService;
    this.teamTaskRepo = teamTaskRepo;
    this.timesheetLineRepo = timesheetLineRepo;
  }

  @Override
  protected void process() {
    AppBusinessProject appBusinessProject = appBusinessProjectService.getAppBusinessProject();

    Map<String, Object> contextValues = null;
    try {
      contextValues = ProjectInvoicingAssistantBatchService.createJsonContext(batch);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    List<Object> updatedTaskList = new ArrayList<Object>();

    String filter =
        !Strings.isNullOrEmpty(appBusinessProject.getExculdeTaskInvoicing())
            ? "self.id NOT IN (SELECT id FROM TeamTask WHERE "
                + appBusinessProject.getExculdeTaskInvoicing()
                + ")"
            : "self.id NOT IN (0)";

    Query<TeamTask> query =
        teamTaskRepo
            .all()
            .filter(
                filter
                    + " AND self.project.isInvoiceable = :invoiceable AND self.toInvoice = :toInvoice")
            .bind("invoiceable", true)
            .bind("toInvoice", false)
            .order("id");

    int offset = 0;
    List<TeamTask> taskList;

    while (!(taskList = query.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();

      for (TeamTask teamTask : taskList) {
        ++offset;
        try {
          teamTask = teamTaskBusinessProjectService.updateTask(teamTask, appBusinessProject);

          if (teamTask.getToInvoice()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", teamTask.getId());
            updatedTaskList.add(map);
          }

          this.updateTimesheetLines(teamTask, contextValues);

        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(IExceptionMessage.BATCH_TASK_UPDATION_1), teamTask.getId()),
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

  private void updateTimesheetLines(TeamTask teamTask, Map<String, Object> contextValues) {

    if (!teamTask.getToInvoice()
        || teamTask.getInvoicingType() != TeamTaskRepository.INVOICING_TYPE_TIME_SPENT) {
      return;
    }
    List<Object> updatedTimesheetLineList = new ArrayList<Object>();

    List<TimesheetLine> timesheetLineList =
        timesheetLineRepo
            .all()
            .filter("self.teamTask.id = :taskId AND self.toInvoice = :toInvoice")
            .bind("taskId", teamTask.getId())
            .bind("toInvoice", false)
            .order("id")
            .fetch();

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
        TraceBackService.trace(e, ExceptionOriginRepository.INVOICE_ORIGIN, batch.getId());
      }
    }
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
