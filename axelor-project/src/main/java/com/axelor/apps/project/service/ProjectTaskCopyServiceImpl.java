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
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskCopyServiceImpl implements ProjectTaskCopyService {

  protected ProjectTaskRepository projectTaskRepository;

  @Inject
  public ProjectTaskCopyServiceImpl(ProjectTaskRepository projectTaskRepository) {
    this.projectTaskRepository = projectTaskRepository;
  }

  @Override
  public List<ProjectTask> copyProjectTaskList(Project sourceProject, Project targetProject) {
    List<ProjectTask> copiedTaskList = new ArrayList<>();
    List<ProjectTask> sourceTaskList = sourceProject.getProjectTaskList();
    if (CollectionUtils.isEmpty(sourceTaskList)) {
      return copiedTaskList;
    }

    Map<Long, ProjectTask> copiedTaskMap = new HashMap<>();
    for (ProjectTask sourceTask : sourceTaskList) {
      ProjectTask copiedTask = projectTaskRepository.copy(sourceTask, false);
      // the copy still points to the tasks of the source project
      copiedTask.setParentTask(null);
      // the ticket number is numbered per project
      copiedTask.setTicketNumber(null);
      targetProject.addProjectTaskListItem(copiedTask);
      copiedTaskMap.put(sourceTask.getId(), copiedTask);
      copiedTaskList.add(copiedTask);
    }

    for (ProjectTask sourceTask : sourceTaskList) {
      ProjectTask sourceParentTask = sourceTask.getParentTask();
      if (sourceParentTask == null) {
        continue;
      }
      // a parent task belonging to another project has no copy: its child becomes a root task
      ProjectTask copiedParentTask = copiedTaskMap.get(sourceParentTask.getId());
      if (copiedParentTask != null) {
        copiedParentTask.addProjectTaskListItem(copiedTaskMap.get(sourceTask.getId()));
      }
    }

    return copiedTaskList;
  }

  @Override
  public void saveProjectTaskList(List<ProjectTask> projectTaskList) {
    for (ProjectTask projectTask : projectTaskList) {
      projectTaskRepository.save(projectTask);
    }
  }
}
