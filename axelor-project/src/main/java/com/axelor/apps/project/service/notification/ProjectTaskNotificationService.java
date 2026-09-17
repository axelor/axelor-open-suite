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
import java.time.LocalDateTime;

public interface ProjectTaskNotificationService {

  /**
   * Notifies the task's assignee, in the notification bell, that the task belongs to the given
   * category. Does nothing if the task has no assignee or no due date, is completed, or was already
   * notified in this category for the same assignee and due date. When {@code
   * reminderFrequencyDays} is positive, notifies again once the last such notification is older
   * than that many days.
   *
   * @return {@code true} if a notification was created.
   */
  boolean notify(
      ProjectTask projectTask,
      ProjectTaskNotificationCategory category,
      int reminderFrequencyDays,
      LocalDateTime now);
}
