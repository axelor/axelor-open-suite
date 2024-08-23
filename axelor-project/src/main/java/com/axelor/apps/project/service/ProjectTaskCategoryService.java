package com.axelor.apps.project.service;

import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import java.util.List;

public interface ProjectTaskCategoryService {
  List<TaskStatusProgressByCategory> getUpdatedProgressList(
      ProjectTaskCategory projectTaskCategory);

  boolean verifyProgressValues(ProjectTaskCategory projectTaskCategory);
}
