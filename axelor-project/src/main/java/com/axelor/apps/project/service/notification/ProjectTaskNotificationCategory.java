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

import com.axelor.apps.project.exception.ProjectExceptionMessage;

public enum ProjectTaskNotificationCategory {
  OVERDUE(
      ProjectExceptionMessage.PROJECT_TASK_NOTIFICATION_OVERDUE,
      ProjectExceptionMessage.PROJECT_TASK_NOTIFICATION_TAG_OVERDUE,
      "important"),
  TODO(
      ProjectExceptionMessage.PROJECT_TASK_NOTIFICATION_TODO,
      ProjectExceptionMessage.PROJECT_TASK_NOTIFICATION_TAG_TODO,
      "warning");

  private final String subjectKey;
  private final String tagTitleKey;
  private final String tagStyle;

  ProjectTaskNotificationCategory(String subjectKey, String tagTitleKey, String tagStyle) {
    this.subjectKey = subjectKey;
    this.tagTitleKey = tagTitleKey;
    this.tagStyle = tagStyle;
  }

  public String getSubjectKey() {
    return subjectKey;
  }

  public String getTagTitleKey() {
    return tagTitleKey;
  }

  public String getTagStyle() {
    return tagStyle;
  }
}
