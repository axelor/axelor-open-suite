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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.util.List;

public interface ProjectTaskCopyService {

  /**
   * Copies the tasks of the source project on the target project, rebuilding the links between the
   * copied tasks so that none of them refers to a task of the source project: parent task,
   * recurrence chain and dependency sets are remapped on the copies. A parent or a next task
   * belonging to another project is dropped, a dependency on another project is kept as is.
   *
   * <p>The copied tasks are only attached to the target project, they are persisted by the cascade
   * with it: this must be called before saving the target project, and {@link
   * #saveCopiedProjectTaskList(List)} must be called after.
   *
   * @param sourceProject the copied project, persisted with its tasks
   * @param targetProject the project to attach the copied tasks to
   * @return the copied tasks
   */
  List<ProjectTask> copyProjectTaskList(Project sourceProject, Project targetProject);

  /**
   * Saves the tasks returned by {@link #copyProjectTaskList(Project, Project)} through their
   * repository, once they are persisted, so that the fields computed on save (full name, ticket
   * number, level indicator, followers) are based on their new ids.
   *
   * <p>Runs in the transaction of the caller, it does not open one.
   *
   * @param copiedTaskList the copied tasks, already persisted
   */
  void saveCopiedProjectTaskList(List<ProjectTask> copiedTaskList);
}
