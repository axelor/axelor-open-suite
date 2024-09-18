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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.businessproject.service.BusinessProjectSprintAllocationLineService;
import com.axelor.apps.hr.db.repo.ProjectPlanningTimeHRRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.service.SprintAllocationLineService;
import com.axelor.inject.Beans;

public class BusinessProjectPlanningTimeRepository extends ProjectPlanningTimeHRRepository {

  @Override
  public ProjectPlanningTime save(ProjectPlanningTime projectPlanningTime) {

    super.save(projectPlanningTime);

    Project project = projectPlanningTime.getProject();
    ProjectTask task = projectPlanningTime.getProjectTask();

    if (project != null && task != null) {
      Sprint sprint = task.getSprint();

      if (sprint != null) {
        Beans.get(BusinessProjectSprintAllocationLineService.class).sprintOnChange(project, sprint);
        Beans.get(SprintAllocationLineService.class).updateSprintTotals(sprint);
      }
    }

    return projectPlanningTime;
  }

  @Override
  public void remove(ProjectPlanningTime projectPlanningTime) {

    Project project = projectPlanningTime.getProject();
    ProjectTask task = projectPlanningTime.getProjectTask();

    super.remove(projectPlanningTime);

    if (project != null && task != null) {
      Sprint sprint = task.getSprint();

      if (sprint != null) {
        Beans.get(BusinessProjectSprintAllocationLineService.class).sprintOnChange(project, sprint);
        Beans.get(SprintAllocationLineService.class).updateSprintTotals(sprint);
      }
    }
  }
}
