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
import com.axelor.apps.hr.utils.ProjectPlanningTimeUtilsService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import java.util.List;

public class ProjectHRRepository extends ProjectManagementRepository {

  protected AppHumanResourceService appHumanResourceService;
  protected ProjectPlanningTimeUtilsService projectPlanningTimeUtilsService;

  @Inject
  public ProjectHRRepository(
      AppHumanResourceService appHumanResourceService,
      ProjectPlanningTimeUtilsService projectPlanningTimeUtilsService) {
    this.appHumanResourceService = appHumanResourceService;
    this.projectPlanningTimeUtilsService = projectPlanningTimeUtilsService;
  }

  @Override
  public Project save(Project project) {
    project = super.save(project);

    if (!appHumanResourceService.isApp("employee")) {
      return project;
    }

    List<ProjectPlanningTime> projectPlanningTimeList =
        JPA.all(ProjectPlanningTime.class)
            .filter("self.project = ?1 OR self.project.parentProject = ?1", project)
            .fetch();

    if (projectPlanningTimeList != null) {
      for (ProjectPlanningTime planningTime : projectPlanningTimeList) {
        ProjectTask task = planningTime.getProjectTask();
        if (task != null) {
          task.setTotalPlannedHrs(projectPlanningTimeUtilsService.getTaskPlannedHrs(task));
        }
      }
    }

    return project;
  }
}
