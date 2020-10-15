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
import com.axelor.apps.project.db.repo.TeamTaskProjectRepository;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class TeamTaskHRRepository extends TeamTaskProjectRepository {

  @Inject private ProjectPlanningTimeService projectPlanningTimeService;

  @Override
  public TeamTask save(TeamTask teamTask) {
    teamTask = super.save(teamTask);

    if (!Beans.get(AppHumanResourceService.class).isApp("employee")
        || teamTask.getProject() == null) {
      return teamTask;
    }

    teamTask.setTotalPlannedHrs(projectPlanningTimeService.getTaskPlannedHrs(teamTask));

    Project project = teamTask.getProject();
    project.setTotalPlannedHrs(projectPlanningTimeService.getProjectPlannedHrs(project));

    Project parentProject = project.getParentProject();
    if (parentProject != null) {
      parentProject.setTotalPlannedHrs(
          projectPlanningTimeService.getProjectPlannedHrs(parentProject));
    }

    return teamTask;
  }

  @Override
  public void remove(TeamTask teamTask) {

    Project project = teamTask.getProject();
    super.remove(teamTask);

    if (project != null) {
      project.setTotalPlannedHrs(projectPlanningTimeService.getProjectPlannedHrs(project));
    }
  }

  @Override
  public TeamTask copy(TeamTask entity, boolean deep) {
    TeamTask task = super.copy(entity, deep);
    task.setTotalPlannedHrs(null);
    task.setTotalRealHrs(null);
    task.clearProjectPlanningTimeList();
    return task;
  }
}
