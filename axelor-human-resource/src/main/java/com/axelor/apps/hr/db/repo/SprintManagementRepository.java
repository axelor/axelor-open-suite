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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.apps.project.service.dashboard.ProjectManagementDashboardService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class SprintManagementRepository extends SprintRepository {
  AppBaseService appBaseService;
  ProjectRepository projectRepository;
  ProjectManagementDashboardService projectManagementDashboardService;
  AllocationLineComputeService allocationLineComputeService;
  static final int DAY_HOURS_NUMBER = 24;

  @Inject
  public SprintManagementRepository(
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      ProjectManagementDashboardService projectManagementDashboardService,
      AllocationLineComputeService allocationLineComputeService) {
    this.appBaseService = appBaseService;
    this.projectRepository = projectRepository;
    this.projectManagementDashboardService = projectManagementDashboardService;
    this.allocationLineComputeService = allocationLineComputeService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long projectId = Long.valueOf(((Map) context.get("project")).get("id").toString());
      Project project = projectRepository.find(projectId);
      Long sprintId = (Long) json.get("id");
      Sprint sprint = Beans.get(SprintRepository.class).find(sprintId);

      final String totalEstimatedTime = "$totalEstimatedTime";
      final String totalAllocatedTime = "$totalAllocatedTime";
      BigDecimal allocatedTime = allocationLineComputeService.getAllocatedTime(project, sprint);
      BigDecimal budgetedTime = getBudgetedTime(sprint);
      json.put(totalAllocatedTime, allocatedTime);
      json.put(totalEstimatedTime, budgetedTime);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return super.populate(json, context);
  }

  protected BigDecimal getBudgetedTime(Sprint sprint) throws AxelorException {

    Long unitHoursId = appBaseService.getUnitHours().getId();
    return sprint.getProjectTaskList().stream()
        .map(
            projectTask -> {
              if (projectTask.getTimeUnit().getId().equals(unitHoursId)) {

                return projectTask
                    .getBudgetedTime()
                    .divide(BigDecimal.valueOf(DAY_HOURS_NUMBER), 2, RoundingMode.HALF_UP);
              }
              return projectTask.getBudgetedTime();
            })
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }
}
