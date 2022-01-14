/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;

public interface ProjectTaskService {

  /** Generates reccurent tasks from given {@link ProjectTask} and {@link Frequency} */
  void generateTasks(ProjectTask projectTask, Frequency frequency);

  /**
   * Updates fields of next task of given {@link ProjectTask}, recursively.
   *
   * <p>This method DOES NOT update potential parent.
   */
  void updateNextTask(ProjectTask projectTask);

  /** Removes all next tasks of given {@link ProjectTask}. */
  void removeNextTasks(ProjectTask projectTask);

  public ProjectTask create(String subject, Project project, User assignedTo);

  void deleteProjectTask(ProjectTask projectTask);
}
