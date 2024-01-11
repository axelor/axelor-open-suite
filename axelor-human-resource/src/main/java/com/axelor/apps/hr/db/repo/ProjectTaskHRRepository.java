/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskProjectRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Map;

public class ProjectTaskHRRepository extends ProjectTaskProjectRepository {

  @Inject private ProjectPlanningTimeService projectPlanningTimeService;

  @Override
  public ProjectTask save(ProjectTask projectTask) {
    projectTask = super.save(projectTask);

    if (!Beans.get(AppHumanResourceService.class).isApp("employee")
        || projectTask.getProject() == null) {
      return projectTask;
    }

    projectTask.setTotalPlannedHrs(projectPlanningTimeService.getTaskPlannedHrs(projectTask));

    Project project = projectTask.getProject();
    project.setTotalPlannedHrs(projectPlanningTimeService.getProjectPlannedHrs(project));

    Project parentProject = project.getParentProject();
    if (parentProject != null) {
      parentProject.setTotalPlannedHrs(
          projectPlanningTimeService.getProjectPlannedHrs(parentProject));
    }

    return projectTask;
  }

  @Override
  public void remove(ProjectTask projectTask) {

    Project project = projectTask.getProject();
    super.remove(projectTask);

    if (project != null) {
      project.setTotalPlannedHrs(projectPlanningTimeService.getProjectPlannedHrs(project));
    }
  }

  @Override
  public ProjectTask copy(ProjectTask entity, boolean deep) {
    ProjectTask task = super.copy(entity, deep);
    task.setTotalPlannedHrs(null);
    task.setTotalRealHrs(null);
    task.clearProjectPlanningTimeList();
    return task;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null
        && json.get("id") != null
        && Boolean.TRUE.equals(context.get("isShowTimeSpent"))) {
      Long id = (Long) json.get("id");
      ProjectTask projectTask = find(id);
      json.put(
          "$durationForCustomer", projectPlanningTimeService.getDurationForCustomer(projectTask));
    }
    return super.populate(json, context);
  }
}
