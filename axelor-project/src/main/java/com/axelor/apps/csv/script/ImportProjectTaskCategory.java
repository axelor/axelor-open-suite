package com.axelor.apps.csv.script;

import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.service.ProjectTaskCategoryService;
import com.google.inject.Inject;
import java.util.Map;

public class ImportProjectTaskCategory {

  @Inject private ProjectTaskCategoryService projectTaskCategoryService;

  public Object importProjectTaskCategory(Object bean, Map<String, Object> values) {
    assert bean instanceof ProjectTaskCategory;
    ProjectTaskCategory projectTaskCategory = (ProjectTaskCategory) bean;

    projectTaskCategory.setTaskStatusProgressByCategoryList(
        projectTaskCategoryService.getUpdatedProgressList(projectTaskCategory));

    return projectTaskCategory;
  }
}
