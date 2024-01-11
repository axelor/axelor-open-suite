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

import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ProjectPlanningTimeHRRepository extends ProjectPlanningTimeRepository {

  @Inject private ProjectPlanningTimeService planningTimeService;

  @Inject private ProjectRepository projectRepo;

  @Override
  public ProjectPlanningTime save(ProjectPlanningTime projectPlanningTime) {

    super.save(projectPlanningTime);

    Project project = projectPlanningTime.getProject();
    project.setTotalPlannedHrs(planningTimeService.getProjectPlannedHrs(project));

    Project parentProject = project.getParentProject();
    if (parentProject != null) {
      parentProject.setTotalPlannedHrs(planningTimeService.getProjectPlannedHrs(parentProject));
    }

    ProjectTask task = projectPlanningTime.getProjectTask();
    if (task != null) {
      task.setTotalPlannedHrs(planningTimeService.getTaskPlannedHrs(task));
    }

    return projectPlanningTime;
  }

  @Override
  public void remove(ProjectPlanningTime projectPlanningTime) {

    Project project = projectPlanningTime.getProject();
    ProjectTask task = projectPlanningTime.getProjectTask();

    super.remove(projectPlanningTime);

    if (task != null) {
      Beans.get(ProjectTaskRepository.class).save(task);
    } else {
      projectRepo.save(project);
    }
  }
}
