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

import com.axelor.apps.hr.utils.ProjectPlanningTimeUtilsService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.google.inject.Inject;

public class ProjectPlanningTimeHRRepository extends ProjectPlanningTimeRepository {

  protected ProjectRepository projectRepo;
  protected ProjectTaskRepository projectTaskRepository;
  protected ProjectPlanningTimeUtilsService projectPlanningTimeUtilsService;

  @Inject
  public ProjectPlanningTimeHRRepository(
      ProjectRepository projectRepo,
      ProjectTaskRepository projectTaskRepository,
      ProjectPlanningTimeUtilsService projectPlanningTimeUtilsService) {
    this.projectRepo = projectRepo;
    this.projectTaskRepository = projectTaskRepository;
    this.projectPlanningTimeUtilsService = projectPlanningTimeUtilsService;
  }

  @Override
  public ProjectPlanningTime save(ProjectPlanningTime projectPlanningTime) {

    super.save(projectPlanningTime);

    ProjectTask task = projectPlanningTime.getProjectTask();
    if (task != null) {
      task.setTotalPlannedHrs(projectPlanningTimeUtilsService.getTaskPlannedHrs(task));
    }

    return projectPlanningTime;
  }

  @Override
  public void remove(ProjectPlanningTime projectPlanningTime) {

    Project project = projectPlanningTime.getProject();
    ProjectTask task = projectPlanningTime.getProjectTask();

    super.remove(projectPlanningTime);

    if (task != null) {
      projectTaskRepository.save(task);
    } else {
      projectRepo.save(project);
    }
  }
}
