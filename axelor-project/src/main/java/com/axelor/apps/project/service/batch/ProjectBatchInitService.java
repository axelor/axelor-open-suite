package com.axelor.apps.project.service.batch;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectBatch;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import java.util.Set;

public interface ProjectBatchInitService {
  ProjectBatch initializeProjectBatchWithProjects(
      Integer actionSelect, Set<Project> projectSet, Set<TaskStatus> taskStatusSet);

  ProjectBatch initializeProjectBatchWithCategories(
      Integer actionSelect,
      Set<ProjectTaskCategory> projectTaskCategorySet,
      Set<TaskStatus> taskStatusSet);
}
