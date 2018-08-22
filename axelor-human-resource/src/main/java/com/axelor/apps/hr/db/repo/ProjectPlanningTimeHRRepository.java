/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class ProjectPlanningTimeHRRepository extends ProjectPlanningTimeRepository {

  @Inject private ProjectPlanningTimeService planningTimeService;

  @Override
  public ProjectPlanningTime save(ProjectPlanningTime projectPlanningTime) {

    projectPlanningTime.setPlannedHours(projectPlanningTime.getPlannedHours());
    planningTimeService.updateTaskPlannedHrs(projectPlanningTime.getTask());
    planningTimeService.updateProjectPlannedHrs(projectPlanningTime.getProject());

    return super.save(projectPlanningTime);
  }

  @Override
  public void remove(ProjectPlanningTime projectPlanningTime) {

    TeamTask task = projectPlanningTime.getTask();
    Project project = projectPlanningTime.getProject();

    super.remove(projectPlanningTime);

    planningTimeService.updateTaskPlannedHrs(task);
    planningTimeService.updateProjectPlannedHrs(project);
  }
}
