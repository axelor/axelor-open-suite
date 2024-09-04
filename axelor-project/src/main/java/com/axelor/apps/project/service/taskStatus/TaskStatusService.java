package com.axelor.apps.project.service.taskStatus;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.studio.db.AppProject;
import java.util.List;

public interface TaskStatusService {
  List<ProjectTask> getProjectTaskToUpdate(AppProject appProject);
}
