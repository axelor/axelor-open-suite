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
package com.axelor.apps.hr.service.allocation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.AllocationLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AllocationLineServiceImpl implements AllocationLineService {

  protected AllocationLineComputeService allocationLineComputeService;
  protected AppBaseService appBaseService;
  protected AllocationLineRepository allocationLineRepository;

  @Inject
  public AllocationLineServiceImpl(
      AllocationLineComputeService allocationLineComputeService,
      AppBaseService appBaseService,
      AllocationLineRepository allocationLineRepository) {
    this.allocationLineComputeService = allocationLineComputeService;
    this.appBaseService = appBaseService;
    this.allocationLineRepository = allocationLineRepository;
  }

  @Override
  public String getEmployeeDomain(Project project) {
    if (project == null || CollectionUtils.isEmpty(project.getMembersUserSet())) {
      return "self.id = 0";
    }
    List<Employee> employeeList =
        project.getMembersUserSet().stream().map(User::getEmployee).collect(Collectors.toList());
    if (CollectionUtils.isEmpty(employeeList)) {
      return "self.id = 0";
    }
    return "self.id IN (" + StringHelper.getIdListString(employeeList) + ")";
  }

  @Override
  public void addAllocationLines(
      Project project,
      List<Employee> employeeList,
      List<Period> periodList,
      BigDecimal allocated,
      boolean initWithPlanningTime)
      throws AxelorException {
    for (Employee employee : employeeList) {
      for (Period period : periodList) {
        createOrUpdateAllocationLine(project, employee, period, allocated, initWithPlanningTime);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createOrUpdateAllocationLine(
      Project project,
      Employee employee,
      Period period,
      BigDecimal allocated,
      boolean initWithPlanningTime)
      throws AxelorException {
    AllocationLine allocationLine =
        allocationLineRepository.findByPeriodAndProjectAndEmployee(period, project, employee);
    if (allocationLine == null) {
      allocationLine = new AllocationLine();
      allocationLine.setEmployee(employee);
      allocationLine.setPeriod(period);
      allocationLine.setProject(project);
    }
    allocationLine.setAllocated(allocated);

    if (initWithPlanningTime) {
      allocationLine.setAllocated(
          allocationLineComputeService.computePlannedTime(
              period.getFromDate(), period.getToDate(), employee, project));
    }
    allocationLineRepository.save(allocationLine);
  }
}
