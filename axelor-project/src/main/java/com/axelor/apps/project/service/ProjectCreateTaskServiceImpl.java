package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Set;

public class ProjectCreateTaskServiceImpl implements ProjectCreateTaskService {

  ProjectTaskService projectTaskService;

  @Inject
  public ProjectCreateTaskServiceImpl(ProjectTaskService projectTaskService) {
    this.projectTaskService = projectTaskService;
  }

  public ProjectTask createTask(
      TaskTemplate taskTemplate, Project project, Set<TaskTemplate> taskTemplateSet) {

    if (!ObjectUtils.isEmpty(project.getProjectTaskList())) {
      for (ProjectTask projectTask : project.getProjectTaskList()) {
        if (projectTask.getName().equals(taskTemplate.getName())) {
          return projectTask;
        }
      }
    }
    ProjectTask task =
        projectTaskService.create(taskTemplate.getName(), project, taskTemplate.getAssignedTo());
    task.setDescription(taskTemplate.getDescription());
    ProjectTaskCategory projectTaskCategory = taskTemplate.getProjectTaskCategory();
    if (projectTaskCategory != null) {
      task.setProjectTaskCategory(projectTaskCategory);
      project.addProjectTaskCategorySetItem(projectTaskCategory);
    }

    TaskTemplate parentTaskTemplate = taskTemplate.getParentTaskTemplate();

    if (parentTaskTemplate != null && taskTemplateSet.contains(parentTaskTemplate)) {
      task.setParentTask(this.createTask(parentTaskTemplate, project, taskTemplateSet));
      return task;
    }
    return task;
  }
}
