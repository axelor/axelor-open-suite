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
import com.axelor.apps.project.service.ProjectTaskToolService;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.mail.MailConstants;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectTaskNotificationServiceImpl implements ProjectTaskNotificationService {

  protected static final String DUE_DATE_TRACK_NAME = "taskEndDate";
  protected static final String DUE_DATE_TRACK_TITLE = "Due Date";
  protected static final String ASSIGNED_TO_ID_KEY = "assignedToId";

  protected final MailMessageRepository mailMessageRepo;
  protected final MailFollowerRepository mailFollowerRepo;
  protected final ProjectTaskToolService projectTaskToolService;
  protected final ObjectMapper objectMapper;

  @Inject
  public ProjectTaskNotificationServiceImpl(
      MailMessageRepository mailMessageRepo,
      MailFollowerRepository mailFollowerRepo,
      ProjectTaskToolService projectTaskToolService,
      ObjectMapper objectMapper) {
    this.mailMessageRepo = mailMessageRepo;
    this.mailFollowerRepo = mailFollowerRepo;
    this.projectTaskToolService = projectTaskToolService;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean notify(
      ProjectTask projectTask,
      ProjectTaskNotificationCategory category,
      int reminderFrequencyDays,
      LocalDateTime now) {
    if (projectTask.getAssignedTo() == null
        || projectTask.getTaskEndDate() == null
        || projectTaskToolService.isCompleted(projectTask)
        || !needsNotification(projectTask, category, reminderFrequencyDays, now)) {
      return false;
    }

    mailFollowerRepo.follow(projectTask, projectTask.getAssignedTo());
    saveMessage(buildMessage(projectTask, category));
    return true;
  }

  protected boolean needsNotification(
      ProjectTask projectTask,
      ProjectTaskNotificationCategory category,
      int reminderFrequencyDays,
      LocalDateTime now) {
    MailMessage lastNotification = findLastNotification(projectTask, category);
    if (lastNotification == null) {
      return true;
    }
    if (reminderFrequencyDays <= 0) {
      return false;
    }

    LocalDateTime lastNotifiedOn = lastNotification.getCreatedOn();
    return lastNotifiedOn != null && lastNotifiedOn.isBefore(now.minusDays(reminderFrequencyDays));
  }

  protected MailMessage findLastNotification(
      ProjectTask projectTask, ProjectTaskNotificationCategory category) {
    return mailMessageRepo
        .all()
        .filter(
            "self.relatedModel = :model AND self.relatedId = :id AND self.type = :type "
                + "AND self.body LIKE :categoryMarker AND self.body LIKE :dueDateMarker "
                + "AND self.body LIKE :assigneeMarker")
        .bind("model", EntityHelper.getEntityClass(projectTask).getName())
        .bind("id", projectTask.getId())
        .bind("type", MailConstants.MESSAGE_TYPE_NOTIFICATION)
        .bind("categoryMarker", contains(toJson(buildTag(category))))
        .bind("dueDateMarker", contains(toJson(buildDueDateTrack(projectTask))))
        .bind(
            "assigneeMarker",
            contains("\"" + ASSIGNED_TO_ID_KEY + "\":\"" + getAssignedToId(projectTask) + "\""))
        .order("-createdOn")
        .fetchOne();
  }

  @Transactional
  protected void saveMessage(MailMessage message) {
    mailMessageRepo.save(message);
  }

  protected MailMessage buildMessage(
      ProjectTask projectTask, ProjectTaskNotificationCategory category) {
    String title = String.format(I18n.get(category.getSubjectKey()), projectTask.getFullName());

    MailMessage message = new MailMessage();
    message.setSubject(title);
    message.setBody(toJson(buildBody(title, projectTask, category)));
    message.setRelatedId(projectTask.getId());
    message.setRelatedModel(EntityHelper.getEntityClass(projectTask).getName());
    message.setRelatedName(projectTask.getFullName());
    message.setType(MailConstants.MESSAGE_TYPE_NOTIFICATION);
    return message;
  }

  protected Map<String, Object> buildBody(
      String title, ProjectTask projectTask, ProjectTaskNotificationCategory category) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("title", title);
    body.put("tags", List.of(buildTag(category)));
    body.put("tracks", List.of(buildDueDateTrack(projectTask)));
    body.put(ASSIGNED_TO_ID_KEY, getAssignedToId(projectTask));
    return body;
  }

  protected Map<String, String> buildTag(ProjectTaskNotificationCategory category) {
    Map<String, String> tag = new LinkedHashMap<>();
    tag.put("title", category.getTagTitleKey());
    tag.put("style", category.getTagStyle());
    return tag;
  }

  protected Map<String, String> buildDueDateTrack(ProjectTask projectTask) {
    Map<String, String> track = new LinkedHashMap<>();
    track.put("name", DUE_DATE_TRACK_NAME);
    track.put("title", DUE_DATE_TRACK_TITLE);
    track.put("value", projectTask.getTaskEndDate().toString());
    return track;
  }

  protected String getAssignedToId(ProjectTask projectTask) {
    return String.valueOf(projectTask.getAssignedTo().getId());
  }

  protected String contains(String fragment) {
    return "%" + fragment + "%";
  }

  protected String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
