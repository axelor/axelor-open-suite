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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.apps.project.service.dashboard.ProjectManagementDashboardService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class SprintManagementRepository extends SprintRepository {
  AppBaseService appBaseService;
  ProjectRepository projectRepository;
  ProjectManagementDashboardService projectManagementDashboardService;
  AllocationLineComputeService allocationLineComputeService;
  UnitConversionForProjectService unitConversionForProjectService;
  EmployeeRepository employeeRepository;
  static final int DAY_HOURS_NUMBER = 24;

  @Inject
  public SprintManagementRepository(
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      ProjectManagementDashboardService projectManagementDashboardService,
      AllocationLineComputeService allocationLineComputeService,
      UnitConversionForProjectService unitConversionForProjectService,
      EmployeeRepository employeeRepository) {
    this.appBaseService = appBaseService;
    this.projectRepository = projectRepository;
    this.projectManagementDashboardService = projectManagementDashboardService;
    this.allocationLineComputeService = allocationLineComputeService;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.employeeRepository = employeeRepository;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long projectId = Long.valueOf(((Map) context.get("project")).get("id").toString());
      Project project = projectRepository.find(projectId);
      Employee employee = null;
      if (context.get("employee") != null) {
        Long emplyeeId = Long.valueOf(((Map) context.get("employee")).get("id").toString());
        employee = employeeRepository.find(emplyeeId);
      }
      Long sprintId = (Long) json.get("id");
      Sprint sprint = Beans.get(SprintRepository.class).find(sprintId);

      final String totalEstimatedTime = "$totalEstimatedTime";
      final String totalAllocatedTime = "$totalAllocatedTime";
      BigDecimal allocatedTime =
          allocationLineComputeService.getAllocatedTime(project, sprint, employee);
      BigDecimal budgetedTime = getBudgetedTime(sprint, project);
      json.put(totalAllocatedTime, allocatedTime);
      json.put(totalEstimatedTime, budgetedTime);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return super.populate(json, context);
  }

  protected BigDecimal getBudgetedTime(Sprint sprint, Project project) throws AxelorException {

    Unit unitHours = appBaseService.getUnitHours();
    Unit unitDays = appBaseService.getUnitDays();
    return sprint.getProjectTaskList().stream()
        .map(
            projectTask -> {
              if (projectTask.getTimeUnit().equals(unitHours)) {
                try {
                  return unitConversionForProjectService.convert(
                      unitHours,
                      unitDays,
                      projectTask.getBudgetedTime(),
                      projectTask.getBudgetedTime().scale(),
                      project);
                } catch (AxelorException e) {
                  throw new RuntimeException(e);
                }
              }
              return projectTask.getBudgetedTime();
            })
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }
}
