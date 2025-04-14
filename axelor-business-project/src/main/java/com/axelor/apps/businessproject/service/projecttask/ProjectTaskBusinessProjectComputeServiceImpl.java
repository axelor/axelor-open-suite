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
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskBusinessProjectComputeServiceImpl
    implements ProjectTaskBusinessProjectComputeService {
  protected UnitConversionForProjectService unitConversionForProjectService;

  @Inject
  public ProjectTaskBusinessProjectComputeServiceImpl(
      UnitConversionForProjectService unitConversionForProjectService) {
    this.unitConversionForProjectService = unitConversionForProjectService;
  }

  @Override
  public void computePlannedTime(ProjectTask projectTask) throws AxelorException {

    BigDecimal plannedTime = BigDecimal.ZERO;
    Unit unitTime = projectTask.getTimeUnit();
    Project project = projectTask.getProject();
    List<ProjectPlanningTime> projectPlanningTimeList = projectTask.getProjectPlanningTimeList();
    if (!CollectionUtils.isEmpty(projectPlanningTimeList)) {
      for (ProjectPlanningTime projectPlanningTime : projectPlanningTimeList) {
        plannedTime =
            plannedTime.add(
                unitConversionForProjectService.convert(
                    projectPlanningTime.getTimeUnit(),
                    unitTime,
                    projectPlanningTime.getPlannedTime(),
                    projectTask.getPlannedTime().scale(),
                    project));
      }
    }
    List<ProjectTask> projectTaskList = projectTask.getProjectTaskList();
    if (!CollectionUtils.isEmpty(projectTaskList)) {
      for (ProjectTask task : projectTaskList) {
        computePlannedTime(task);
        plannedTime = plannedTime.add(task.getPlannedTime());
      }
    }

    projectTask.setPlannedTime(plannedTime);
  }
}
