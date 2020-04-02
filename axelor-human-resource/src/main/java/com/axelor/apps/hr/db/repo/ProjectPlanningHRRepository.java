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

import com.axelor.apps.hr.service.project.ProjectPlanningService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class ProjectPlanningHRRepository extends ProjectPlanningRepository {

  @Inject private ProjectPlanningService planningService;

  @Override
  public ProjectPlanning save(ProjectPlanning projectPlanning) {

    try {
      if (!projectPlanning.getFromEditor()) {
        projectPlanning = planningService.updatePlanningTime(projectPlanning);
      }
      projectPlanning.setTotalPlannedHrs(planningService.getTotalPlannedHrs(projectPlanning));
      planningService.updateTaskPlannedHrs(projectPlanning.getTask());
      planningService.updateProjectPlannedHrs(projectPlanning.getProject());
    } catch (AxelorException e) {
      e.printStackTrace();
    }

    return super.save(projectPlanning);
  }

  @Override
  public void remove(ProjectPlanning projectPlanning) {

    TeamTask task = projectPlanning.getTask();
    Project project = projectPlanning.getProject();

    super.remove(projectPlanning);

    planningService.updateTaskPlannedHrs(task);
    planningService.updateProjectPlannedHrs(project);
  }
}
