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
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.notification.ProjectTaskNotificationCategory;
import com.axelor.apps.project.service.notification.ProjectTaskNotificationService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BatchProjectTaskNotification extends BatchStrategy {

  protected static final int DEFAULT_TO_DO_ANTICIPATION_DAYS = 7;
  protected static final int DEFAULT_OVERDUE_LOOKBACK_DAYS = 30;

  protected final ProjectTaskRepository projectTaskRepo;
  protected final ProjectTaskNotificationService projectTaskNotificationService;

  protected LocalDate today;
  protected LocalDateTime now;

  @Inject
  public BatchProjectTaskNotification(
      ProjectTaskRepository projectTaskRepo,
      ProjectTaskNotificationService projectTaskNotificationService) {
    this.projectTaskRepo = projectTaskRepo;
    this.projectTaskNotificationService = projectTaskNotificationService;
  }

  @Override
  protected void process() {
    ProjectBatch projectBatch = batch.getProjectBatch();
    today = appBaseService.getTodayDate(null);
    now = appBaseService.getTodayDateTime(null).toLocalDateTime();

    notify(
        getOverdueTaskQuery(
            positiveOrDefault(
                projectBatch.getOverdueLookbackDays(), DEFAULT_OVERDUE_LOOKBACK_DAYS)),
        ProjectTaskNotificationCategory.OVERDUE,
        positiveOrDefault(projectBatch.getOverdueReminderFrequencyDays(), 0));
    notify(
        getToDoTaskQuery(
            positiveOrDefault(
                projectBatch.getToDoAnticipationDays(), DEFAULT_TO_DO_ANTICIPATION_DAYS)),
        ProjectTaskNotificationCategory.TODO,
        0);
  }

  protected Query<ProjectTask> getOverdueTaskQuery(int lookbackDays) {
    return projectTaskRepo
        .all()
        .filter(
            "self.assignedTo IS NOT NULL AND self.taskEndDate >= :fromDate "
                + "AND self.taskEndDate < :today")
        .bind("fromDate", today.minusDays(lookbackDays))
        .bind("today", today)
        .order("id");
  }

  protected Query<ProjectTask> getToDoTaskQuery(int anticipationDays) {
    return projectTaskRepo
        .all()
        .filter(
            "self.assignedTo IS NOT NULL AND self.taskEndDate >= :today "
                + "AND self.taskEndDate <= :toDate")
        .bind("today", today)
        .bind("toDate", today.plusDays(anticipationDays))
        .order("id");
  }

  protected int positiveOrDefault(Integer value, int defaultValue) {
    return value != null && value > 0 ? value : defaultValue;
  }

  protected void notify(
      Query<ProjectTask> projectTaskQuery,
      ProjectTaskNotificationCategory category,
      int reminderFrequencyDays) {
    int offset = 0;
    List<ProjectTask> projectTaskList;

    while (!(projectTaskList = projectTaskQuery.fetch(getFetchLimit(), offset)).isEmpty()) {
      for (ProjectTask projectTask : projectTaskList) {
        offset++;

        try {
          if (projectTaskNotificationService.notify(
              projectTask, category, reminderFrequencyDays, now)) {
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
