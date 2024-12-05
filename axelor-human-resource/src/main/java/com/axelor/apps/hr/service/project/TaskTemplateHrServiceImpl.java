package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.service.TaskTemplateServiceImpl;

public class TaskTemplateHrServiceImpl extends TaskTemplateServiceImpl {

  @Override
  public void manageTemplateFields(ProjectTask task, TaskTemplate taskTemplate, Project project)
      throws AxelorException {
    super.manageTemplateFields(task, taskTemplate, project);

    task.setTotalPlannedHrs(taskTemplate.getTotalPlannedHrs());
  }
}
