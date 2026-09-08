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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
      // the recurrence chain is copied as is, the copy must not generate a new one
      copiedTask.setIsFirst(false);
      // a sprint belongs to the source project and is not copied with it
      copiedTask.setActiveSprint(null);
      copiedTask.setOldActiveSprint(null);
      targetProject.addProjectTaskListItem(copiedTask);
      copiedTaskMap.put(sourceTask.getId(), copiedTask);
      copiedTaskList.add(copiedTask);
    }

    for (ProjectTask sourceTask : sourceTaskList) {
      relinkCopiedTask(sourceTask, copiedTaskMap);
    }

    return copiedTaskList;
  }

  @Override
  public void saveCopiedProjectTaskList(List<ProjectTask> copiedTaskList) {
    // each save recomputes the levels of the whole task tree of the project, which is quadratic on
    // the number of copied tasks, but it is the only way to apply the whole task save process
    for (ProjectTask copiedTask : copiedTaskList) {
      projectTaskRepository.save(copiedTask);
    }
  }

  /**
   * Replaces on the copied task the links that still refer to the tasks of the source project.
   *
   * @param sourceTask the copied task
   * @param copiedTaskMap the copies, by id of the source task
   */
  protected void relinkCopiedTask(ProjectTask sourceTask, Map<Long, ProjectTask> copiedTaskMap) {
    ProjectTask copiedTask = copiedTaskMap.get(sourceTask.getId());

    // a parent task belonging to another project has no copy: its child becomes a root task
    ProjectTask copiedParentTask = getCopiedTask(sourceTask.getParentTask(), copiedTaskMap);
    if (copiedParentTask != null) {
      copiedParentTask.addProjectTaskListItem(copiedTask);
    }

    // a recurrence chain never crosses a project, it is dropped when it is not copied
    copiedTask.setNextProjectTask(getCopiedTask(sourceTask.getNextProjectTask(), copiedTaskMap));

    copiedTask.setFinishToStartSet(copyTaskSet(sourceTask.getFinishToStartSet(), copiedTaskMap));
    copiedTask.setStartToStartSet(copyTaskSet(sourceTask.getStartToStartSet(), copiedTaskMap));
    copiedTask.setFinishToFinishSet(copyTaskSet(sourceTask.getFinishToFinishSet(), copiedTaskMap));
  }

  /**
   * Returns the set of tasks to set on a copy, where every task of the source project is replaced
   * by its copy. A dependency on the task of another project is a real one, it is kept as is.
   *
   * @param sourceTaskSet the dependencies of the copied task
   * @param copiedTaskMap the copies, by id of the source task
   * @return a new set holding the dependencies of the copy
   */
  protected Set<ProjectTask> copyTaskSet(
      Set<ProjectTask> sourceTaskSet, Map<Long, ProjectTask> copiedTaskMap) {
    if (sourceTaskSet == null) {
      return null;
    }

    // never give the collection of the source task to its copy, Hibernate rejects a collection
    // shared by two entities
    Set<ProjectTask> taskSet = new HashSet<>();
    for (ProjectTask sourceTask : sourceTaskSet) {
      ProjectTask copiedTask = getCopiedTask(sourceTask, copiedTaskMap);
      taskSet.add(copiedTask != null ? copiedTask : sourceTask);
    }
    return taskSet;
  }

  protected ProjectTask getCopiedTask(
      ProjectTask sourceTask, Map<Long, ProjectTask> copiedTaskMap) {
    return sourceTask != null ? copiedTaskMap.get(sourceTask.getId()) : null;
  }
}
