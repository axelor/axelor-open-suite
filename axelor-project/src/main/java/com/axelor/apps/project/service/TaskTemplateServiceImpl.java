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
package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskTemplate;
import java.util.Set;

public class TaskTemplateServiceImpl implements TaskTemplateService {

  @Override
  public Set<TaskTemplate> getParentTaskTemplateFromTaskTemplate(
      TaskTemplate taskTemplate, Set<TaskTemplate> taskTemplateSet) {
    if (taskTemplate == null || taskTemplateSet.contains(taskTemplate)) {
      return taskTemplateSet;
    }
    taskTemplateSet.add(taskTemplate);
    taskTemplateSet.addAll(
        this.getParentTaskTemplateFromTaskTemplate(
            taskTemplate.getParentTaskTemplate(), taskTemplateSet));
    return taskTemplateSet;
  }

  @Override
  public boolean isParentTaskTemplateCreatedLoop(
      TaskTemplate taskTemplate, TaskTemplate parentTaskTemplate) {

    if (parentTaskTemplate == null) {
      return false;
    } else if (taskTemplate.equals(parentTaskTemplate)) {
      return true;
    }
    return isParentTaskTemplateCreatedLoop(
        taskTemplate, parentTaskTemplate.getParentTaskTemplate());
  }

  @Override
  public void manageTemplateFields(ProjectTask task, TaskTemplate taskTemplate, Project project)
      throws AxelorException {
    task.setDescription(taskTemplate.getDescription());
    task.setTaskDuration(taskTemplate.getDuration().intValue());

    task.setProduct(taskTemplate.getProduct());
    task.setQuantity(taskTemplate.getQty());

    ProjectTaskCategory projectTaskCategory = taskTemplate.getProjectTaskCategory();
    if (projectTaskCategory != null) {
      task.setProjectTaskCategory(projectTaskCategory);
      project.addProjectTaskCategorySetItem(projectTaskCategory);
    }
  }
}
