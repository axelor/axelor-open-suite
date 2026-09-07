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

public interface ProjectTaskNotificationService {

  /**
   * Notifies the task's assignee, in the notification bell, that the task is overdue. Does nothing
   * if the task has no assignee or was already notified as overdue.
   *
   * @return {@code true} if a notification was created.
   */
  boolean notifyOverdueTask(ProjectTask projectTask);

  /**
   * Notifies the task's assignee, in the notification bell, that the task is to do. Does nothing if
   * the task has no assignee or was already notified as to do.
   *
   * @return {@code true} if a notification was created.
   */
  boolean notifyToDoTask(ProjectTask projectTask);
}
