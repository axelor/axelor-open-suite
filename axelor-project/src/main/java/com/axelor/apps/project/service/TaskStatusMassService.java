package com.axelor.apps.project.service;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.studio.db.AppProject;
import java.util.List;

public interface TaskStatusMassService {
  Integer updateTaskStatusOnProjectTask(
      List<ProjectTask> projectTaskList, AppProject appProject, ProjectTaskCategory category);
}
