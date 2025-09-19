/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.ProjectTaskComputeServiceImpl;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.google.inject.Inject;

public class ProjectTaskComputeBusinessProjectServiceImpl extends ProjectTaskComputeServiceImpl
    implements ProjectTaskComputeBusinessProjectService {

  @Inject
  public ProjectTaskComputeBusinessProjectServiceImpl(
      ProjectTimeUnitService projectTimeUnitService,
      UnitConversionForProjectService unitConversionForProjectService) {
    super(projectTimeUnitService, unitConversionForProjectService);
  }

  @Override
  public void computeSoldTime(ProjectTask projectTask, Unit oldTimeUnit) throws AxelorException {
    Unit unit = projectTimeUnitService.getTaskDefaultHoursTimeUnit(projectTask);
    if (projectTask == null
        || oldTimeUnit == null
        || unit == null
        || projectTask.getProject() == null) {
      return;
    }
    projectTask.setSoldTime(
        unitConversionForProjectService.convert(
            oldTimeUnit, unit, projectTask.getSoldTime(), COMPUTE_SCALE, projectTask.getProject()));
  }
}
