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
package com.axelor.apps.businesssupport.db.repo;

import com.axelor.apps.businessproject.db.repo.ProjectTaskBusinessProjectRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.utils.ProjectTaskUtilsService;
import com.google.inject.Inject;

public class ProjectTaskBusinessSupportRepository extends ProjectTaskBusinessProjectRepository {

  @Inject
  public ProjectTaskBusinessSupportRepository(
      AppProjectService appProjectService, ProjectTaskUtilsService projectTaskUtilsService) {
    super(appProjectService, projectTaskUtilsService);
  }

  @Override
  public ProjectTask copy(ProjectTask entity, boolean deep) {
    ProjectTask task = super.copy(entity, deep);
    task.setTargetVersion(null);
    return task;
  }
}
