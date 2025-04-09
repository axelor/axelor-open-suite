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

import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskBusinessProjectComputeServiceImpl
    implements ProjectTaskBusinessProjectComputeService {
  @Override
  public BigDecimal computePlannedTime(ProjectTask projectTask) {

    BigDecimal plannedTime = BigDecimal.ZERO;
    plannedTime =
        projectTask.getProjectPlanningTimeList().stream()
            .map(ProjectPlanningTime::getPlannedTime)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    List<ProjectTask> projectTaskList = projectTask.getProjectTaskList();
    if (!CollectionUtils.isEmpty(projectTaskList)) {
      for (ProjectTask task : projectTaskList) {
        computePlannedTime(task);
        plannedTime = plannedTime.add(task.getPlannedTime());
      }
    }
    projectTask.setPlannedTime(plannedTime);
    return plannedTime;
  }
}
