/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service.batch;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.batch.BatchStrategy;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.service.notification.ProjectTaskNotificationService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppProject;
import jakarta.inject.Inject;
import java.util.List;

public class BatchProjectTaskNotification extends BatchStrategy {

  protected static final int DEFAULT_ANTICIPATION_DAYS = 7;

  protected ProjectTaskRepository projectTaskRepo;
  protected ProjectTaskNotificationService projectTaskNotificationService;
  protected AppProjectService appProjectService;

  @Inject
  public BatchProjectTaskNotification(
      ProjectTaskRepository projectTaskRepo,
      ProjectTaskNotificationService projectTaskNotificationService,
      AppProjectService appProjectService) {
    this.projectTaskRepo = projectTaskRepo;
    this.projectTaskNotificationService = projectTaskNotificationService;
    this.appProjectService = appProjectService;
  }

  @Override
  protected void process() {
    findBatch();
    notify(getOverdueTasks(), true);
    notify(getToDoTasks(), false);
  }

  protected Query<ProjectTask> getOverdueTasks() {
    return projectTaskRepo
        .all()
        .filter(
            "self.assignedTo IS NOT NULL AND self.taskEndDate IS NOT NULL "
                + "AND self.taskEndDate < CURRENT_DATE")
        .order("id");
  }

  protected Query<ProjectTask> getToDoTasks() {
    return projectTaskRepo
        .all()
        .filter(
            "self.assignedTo IS NOT NULL AND self.taskEndDate IS NOT NULL "
                + "AND self.taskEndDate >= CURRENT_DATE AND self.taskEndDate <= :windowEnd")
        .bind("windowEnd", appBaseService.getTodayDate(null).plusDays(getAnticipationDays()))
        .order("id");
  }

  protected int getAnticipationDays() {
    AppProject appProject = appProjectService.getAppProject();
    Integer anticipationDays =
        appProject != null ? appProject.getTaskNotificationAnticipationDays() : null;
    return anticipationDays != null && anticipationDays >= 0
        ? anticipationDays
        : DEFAULT_ANTICIPATION_DAYS;
  }

  protected void notify(Query<ProjectTask> projectTaskQuery, boolean overdue) {
    int offset = 0;
    List<ProjectTask> projectTaskList;

    while (!(projectTaskList = projectTaskQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      for (ProjectTask projectTask : projectTaskList) {
        offset++;

        try {
          boolean notified =
              overdue
                  ? projectTaskNotificationService.notifyOverdueTask(projectTask)
                  : projectTaskNotificationService.notifyToDoTask(projectTask);
          if (notified) {
            incrementDone();
          }
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(
              e,
              String.format(
                  I18n.get(ProjectExceptionMessage.BATCH_TASK_STATUS_UPDATE_TASK), projectTask),
              batch.getId());
        }
      }
      JPA.clear();
      findBatch();
    }
  }

  @Override
  protected void stop() {
    String comment = I18n.get(ProjectExceptionMessage.BATCH_TASK_NOTIFICATION_1);
    comment +=
        String.format(
            "\t" + I18n.get(ProjectExceptionMessage.BATCH_TASK_NOTIFICATION_DONE), batch.getDone());
    comment +=
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
