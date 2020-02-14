/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import java.util.List;

public class ProjectHRRepository extends ProjectManagementRepository {

  @Inject private ProjectPlanningTimeService projectPlanningTimeService;

  @Inject private ProjectPlanningTimeRepository planningTimeRepo;

  @Override
  public Project save(Project project) {
    project = super.save(project);

    if (!Beans.get(AppHumanResourceService.class).isApp("employee")) {
      return project;
    }

    List<ProjectPlanningTime> projectPlanningTimeList =
        planningTimeRepo
            .all()
            .filter("self.project = ?1 OR self.project.parentProject = ?1", project)
            .fetch();

    project.setTotalPlannedHrs(projectPlanningTimeService.getProjectPlannedHrs(project));

    Project parentProject = project.getParentProject();
    if (parentProject != null) {
      parentProject.setTotalPlannedHrs(
          projectPlanningTimeService.getProjectPlannedHrs(parentProject));
    }

    if (projectPlanningTimeList != null) {
      for (ProjectPlanningTime planningTime : projectPlanningTimeList) {
        TeamTask task = planningTime.getTask();
        if (task != null) {
          task.setTotalPlannedHrs(projectPlanningTimeService.getTaskPlannedHrs(task));
        }
      }
    }

    return project;
  }
}
