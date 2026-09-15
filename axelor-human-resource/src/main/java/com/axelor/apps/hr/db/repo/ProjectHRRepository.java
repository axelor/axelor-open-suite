/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.inject.Beans;
import jakarta.inject.Inject;

public class ProjectHRRepository extends ProjectManagementRepository {

  @Inject private ProjectPlanningTimeService projectPlanningTimeService;

  @Override
  public Project save(Project project) {
    project = super.save(project);
    updateProjectPlanningTimes(project);
    return project;
  }

  protected void updateProjectPlanningTimes(Project project) {
    if (!Beans.get(AppHumanResourceService.class).isApp("employee")) {
      return;
    }

    projectPlanningTimeService
        .getPlannedHrsByTask(project)
        .forEach(ProjectTask::setTotalPlannedHrs);
  }
}
