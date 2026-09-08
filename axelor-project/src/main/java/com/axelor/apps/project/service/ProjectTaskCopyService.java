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
   * Copies the tasks of the source project on the target project, rebuilding the parent/child
   * hierarchy between the copied tasks. A parent task belonging to another project is dropped, its
   * copy becomes a root task. The copied tasks are only attached to the target project: they are
   * persisted with it, so this must be called before saving the target project.
   *
   * @param sourceProject the copied project
   * @param targetProject the project to attach the copied tasks to
   * @return the copied tasks
   */
  List<ProjectTask> copyProjectTaskList(Project sourceProject, Project targetProject);

  /**
   * Saves the given tasks through their repository, so that the fields computed on save (full name,
   * ticket number, level indicator, followers) are based on their new ids. To be called once the
   * tasks are persisted.
   *
   * @param projectTaskList the tasks to save
   */
  void saveProjectTaskList(List<ProjectTask> projectTaskList);
}
