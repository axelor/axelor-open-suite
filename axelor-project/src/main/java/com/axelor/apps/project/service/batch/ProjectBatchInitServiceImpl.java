/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service.batch;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.ProjectBatchRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.Set;

public class ProjectBatchInitServiceImpl implements ProjectBatchInitService {

  protected ProjectBatchRepository projectBatchRepository;

  @Inject
  public ProjectBatchInitServiceImpl(ProjectBatchRepository projectBatchRepository) {
    this.projectBatchRepository = projectBatchRepository;
  }

  @Override
  public ProjectBatch initializeProjectBatchWithProjects(
      Integer actionSelect, Set<Project> projectSet, Set<TaskStatus> taskStatusSet) {
    return initializeProjectBatch(actionSelect, projectSet, new HashSet<>(), taskStatusSet);
  }

  @Override
  public ProjectBatch initializeProjectBatchWithCategories(
      Integer actionSelect,
      Set<ProjectTaskCategory> projectTaskCategorySet,
      Set<TaskStatus> taskStatusSet) {
    return initializeProjectBatch(
        actionSelect, new HashSet<>(), projectTaskCategorySet, taskStatusSet);
  }

  @Transactional(rollbackOn = Exception.class)
  protected ProjectBatch initializeProjectBatch(
      Integer actionSelect,
      Set<Project> projectSet,
      Set<ProjectTaskCategory> projectTaskCategorySet,
      Set<TaskStatus> taskStatusSet) {
    ProjectBatch projectBatch = new ProjectBatch();
    projectBatch.setActionSelect(actionSelect);
    projectBatch.setProjectSet(projectSet);
    projectBatch.setTaskCategorySet(projectTaskCategorySet);
    projectBatch.setTaskStatusSet(taskStatusSet);

    projectBatchRepository.save(projectBatch);

    return projectBatch;
  }
}
