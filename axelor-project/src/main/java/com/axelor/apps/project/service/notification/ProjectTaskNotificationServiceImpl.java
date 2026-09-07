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
package com.axelor.apps.project.service.notification;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.ProjectTaskToolService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.mail.MailConstants;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.studio.db.AppProject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProjectTaskNotificationServiceImpl implements ProjectTaskNotificationService {

  protected static final String OVERDUE_NOTIFICATION_CATEGORY = "OVERDUE";
  protected static final String TODO_NOTIFICATION_CATEGORY = "TODO";

  protected final MailMessageRepository mailMessageRepo;
  protected final MailFollowerRepository mailFollowerRepo;
  protected final ProjectTaskToolService projectTaskToolService;
  protected final AppProjectService appProjectService;

  @Inject
  public ProjectTaskNotificationServiceImpl(
      MailMessageRepository mailMessageRepo,
      MailFollowerRepository mailFollowerRepo,
      ProjectTaskToolService projectTaskToolService,
      AppProjectService appProjectService) {
    this.mailMessageRepo = mailMessageRepo;
    this.mailFollowerRepo = mailFollowerRepo;
    this.projectTaskToolService = projectTaskToolService;
    this.appProjectService = appProjectService;
  }

  @Override
  public boolean notifyOverdueTask(ProjectTask projectTask) {
    if (projectTask.getAssignedTo() == null || projectTaskToolService.isCompleted(projectTask)) {
      return false;
    }

    String subject =
        String.format(
            I18n.get(ProjectExceptionMessage.PROJECT_TASK_NOTIFICATION_OVERDUE),
            projectTask.getFullName());

    if (!needsOverdueNotification(projectTask)) {
      return false;
    }

    notify(projectTask, subject, OVERDUE_NOTIFICATION_CATEGORY);
    return true;
  }

  @Override
  public boolean notifyToDoTask(ProjectTask projectTask) {
    if (projectTask.getAssignedTo() == null || projectTaskToolService.isCompleted(projectTask)) {
      return false;
    }

    String subject =
        String.format(
            I18n.get(ProjectExceptionMessage.PROJECT_TASK_NOTIFICATION_TODO),
            projectTask.getFullName());

    if (findLastNotification(projectTask, TODO_NOTIFICATION_CATEGORY) != null) {
      return false;
    }

    notify(projectTask, subject, TODO_NOTIFICATION_CATEGORY);
    return true;
  }

  /**
   * Notify once when a task first becomes overdue; if {@code
   * AppProject.taskOverdueReminderFrequencyDays} is set, notify again once the last overdue
   * notification is older than that many days.
   */
  protected boolean needsOverdueNotification(ProjectTask projectTask) {
    MailMessage lastNotification = findLastNotification(projectTask, OVERDUE_NOTIFICATION_CATEGORY);
    if (lastNotification == null) {
      return true;
    }

    Integer reminderFrequencyDays = getOverdueReminderFrequencyDays();
    if (reminderFrequencyDays == null || reminderFrequencyDays <= 0) {
      return false;
    }

    LocalDateTime lastNotifiedOn = lastNotification.getCreatedOn();
    return lastNotifiedOn != null
        && lastNotifiedOn.isBefore(LocalDateTime.now().minusDays(reminderFrequencyDays));
  }

  protected Integer getOverdueReminderFrequencyDays() {
    AppProject appProject = appProjectService.getAppProject();
    return appProject != null ? appProject.getTaskOverdueReminderFrequencyDays() : null;
  }

  protected void notify(ProjectTask projectTask, String subject, String category) {
    User assignedTo = projectTask.getAssignedTo();
    mailFollowerRepo.follow(projectTask, assignedTo);
    saveMessage(buildMessage(projectTask, subject, category));
  }

  @Transactional
  protected void saveMessage(MailMessage message) {
    mailMessageRepo.save(message);
  }

  protected MailMessage findLastNotification(ProjectTask projectTask, String category) {
    return mailMessageRepo
        .all()
        .filter(
            "self.relatedModel = :model AND self.relatedId = :id "
                + "AND self.type = :type AND self.body LIKE :categoryMarker")
        .bind("model", EntityHelper.getEntityClass(projectTask).getName())
        .bind("id", projectTask.getId())
        .bind("type", MailConstants.MESSAGE_TYPE_NOTIFICATION)
        .bind("categoryMarker", "%\"category\":\"" + category + "\"%")
        .order("-createdOn")
        .fetchOne();
  }

  protected MailMessage buildMessage(ProjectTask projectTask, String subject, String category) {
    MailMessage message = new MailMessage();
    message.setSubject(subject);
    message.setBody(toJSON(subject, category));
    message.setRelatedId(projectTask.getId());
    message.setRelatedModel(EntityHelper.getEntityClass(projectTask).getName());
    message.setRelatedName(projectTask.getFullName());
    message.setType(MailConstants.MESSAGE_TYPE_NOTIFICATION);
    return message;
  }

  protected String toJSON(String title, String category) {
    Map<String, Object> json = new LinkedHashMap<>();
    json.put("title", title);
    json.put("category", category);
    json.put("tags", new ArrayList<>());
    json.put("tracks", new ArrayList<>());
    try {
      return Beans.get(ObjectMapper.class).writeValueAsString(json);
    } catch (Exception e) {
      return title;
    }
  }
}
