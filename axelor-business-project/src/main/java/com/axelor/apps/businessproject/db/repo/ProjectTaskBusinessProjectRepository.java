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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.ProjectTaskProgressUpdateService;
import com.axelor.apps.hr.db.repo.ProjectTaskHRRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.utils.ProjectPlanningTimeUtilsService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Collections;
import javax.persistence.PersistenceException;

public class ProjectTaskBusinessProjectRepository extends ProjectTaskHRRepository {

  @Inject
  public ProjectTaskBusinessProjectRepository(
      AppHumanResourceService appHumanResourceService,
      ProjectPlanningTimeUtilsService projectPlanningTimeUtilsService) {
    super(appHumanResourceService, projectPlanningTimeUtilsService);
  }

  @Override
  public ProjectTask copy(ProjectTask entity, boolean deep) {
    ProjectTask task = super.copy(entity, deep);
    task.setSaleOrderLine(null);
    task.setInvoiceLineSet(Collections.emptySet());
    return task;
  }

  @Override
  public ProjectTask save(ProjectTask projectTask) {
    projectTask = super.save(projectTask);
    ProjectTaskProgressUpdateService projectTaskProgressUpdateService =
        Beans.get(ProjectTaskProgressUpdateService.class);
    try {
      projectTask =
          projectTaskProgressUpdateService.updateChildrenProgress(
              projectTask, projectTask.getProgress());
      projectTask = projectTaskProgressUpdateService.updateParentsProgress(projectTask);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    return projectTask;
  }
}
