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

import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class ProjectHRRepository extends ProjectManagementRepository {

  @Inject private ProjectPlanningTimeService projectPlanningTimeService;

  @Override
  public Project save(Project project) {
    super.save(project);

    project.setTotalPlannedHrs(projectPlanningTimeService.getProjectPlannedHrs(project));
    project.setTotalRealHrs(projectPlanningTimeService.getProjectRealHrs(project));

    if (project.getProjectPlanningTimeList() != null) {
      for (ProjectPlanningTime planningTime : project.getProjectPlanningTimeList()) {
        TeamTask task = planningTime.getTask();
        if (task != null) {
          task.setTotalPlannedHrs(projectPlanningTimeService.getTaskPlannedHrs(task));
          task.setTotalRealHrs(projectPlanningTimeService.getTaskRealHrs(task));
        }
      }
    }

    return project;
  }
}
