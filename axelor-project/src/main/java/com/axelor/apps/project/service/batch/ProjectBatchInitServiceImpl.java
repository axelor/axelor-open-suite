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
