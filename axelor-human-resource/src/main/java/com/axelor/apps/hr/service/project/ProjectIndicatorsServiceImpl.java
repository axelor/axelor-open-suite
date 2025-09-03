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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProjectIndicatorsServiceImpl implements ProjectIndicatorsService {
  protected AllocationLineComputeService allocationLineComputeService;
  protected ProjectTaskRepository projectTaskRepository;

  @Inject
  public ProjectIndicatorsServiceImpl(
      AllocationLineComputeService allocationLineComputeService,
      ProjectTaskRepository projectTaskRepository) {
    this.allocationLineComputeService = allocationLineComputeService;
    this.projectTaskRepository = projectTaskRepository;
  }

  @Override
  public BigDecimal getProjectOrEmployeeLeaveDays(
      Project project, Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    if (employee != null) {
      return allocationLineComputeService.getLeaves(fromDate, toDate, employee);
    }
    BigDecimal leaveDaysSum = BigDecimal.ZERO;
    if (project != null) {
      Set<User> userSet = project.getMembersUserSet();
      for (User user : userSet) {
        if (user.getEmployee() != null) {
          leaveDaysSum =
              leaveDaysSum.add(
                  allocationLineComputeService.getLeaves(fromDate, toDate, user.getEmployee()));
        }
      }
    }
    return leaveDaysSum;
  }

  @Override
  public BigDecimal getAvailableDays(
      Project project, Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    BigDecimal availableAllocation = BigDecimal.ZERO;
    if (employee != null) {
      BigDecimal leave = allocationLineComputeService.getLeaves(fromDate, toDate, employee);
      BigDecimal allocatedTime =
          allocationLineComputeService.getAllocatedTime(null, fromDate, toDate, employee);
      return allocationLineComputeService.getAvailableAllocation(
          fromDate, toDate, employee, leave, allocatedTime);
    }
    if (project != null) {
      for (User user : project.getMembersUserSet()) {

        if (user != null) {
          Employee userEmployee = user.getEmployee();
          BigDecimal leave = allocationLineComputeService.getLeaves(fromDate, toDate, userEmployee);
          BigDecimal allocatedTime =
              allocationLineComputeService.getAllocatedTime(null, fromDate, toDate, userEmployee);
          availableAllocation =
              availableAllocation.add(
                  allocationLineComputeService.getAvailableAllocation(
                      fromDate, toDate, userEmployee, leave, allocatedTime));
        }
      }
    }
    return availableAllocation;
  }

  @Override
  public BigDecimal getEstimatedTime(
      Project project, Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    List<ProjectTask> projectTaskList = new ArrayList<>();
    if (project != null && employee != null) {
      projectTaskList =
          projectTaskRepository
              .findByEmployeeProjectAndPeriod(employee, project, fromDate, toDate)
              .fetch();
    }
    if (project != null && employee == null) {
      projectTaskList =
          projectTaskRepository.findByProjectAndPeriod(project, fromDate, toDate).fetch();
    }
    if (project == null && employee != null) {
      projectTaskList =
          projectTaskRepository.findByEmployeeAndPeriod(employee, fromDate, toDate).fetch();
    }
    return allocationLineComputeService.getBudgetedTime(projectTaskList, project);
  }
}
