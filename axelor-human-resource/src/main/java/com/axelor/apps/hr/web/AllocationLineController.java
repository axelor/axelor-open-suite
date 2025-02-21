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
package com.axelor.apps.hr.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.apps.hr.service.allocation.AllocationLineService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AllocationLineController {

  public void setEmployeeDomain(ActionRequest request, ActionResponse response) {
    AllocationLine allocationLine = request.getContext().asType(AllocationLine.class);
    response.setAttr(
        "employee",
        "domain",
        Beans.get(AllocationLineService.class).getEmployeeDomain(allocationLine.getProject()));
  }

  @SuppressWarnings("unchecked")
  public void setEmployeeSetDomain(ActionRequest request, ActionResponse response) {
    if (request.getContext().get("_project") == null) {
      return;
    }
    LinkedHashMap<String, Object> projectMap =
        (LinkedHashMap<String, Object>) request.getContext().get("_project");
    Project project =
        Beans.get(ProjectRepository.class).find(Long.parseLong(projectMap.get("id").toString()));
    response.setAttr(
        "$employeeSet",
        "domain",
        Beans.get(AllocationLineService.class).getEmployeeDomain(project));
  }

  @SuppressWarnings("unchecked")
  public void addAllocationLines(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    if (context.get("_project") == null) {
      return;
    }
    LinkedHashMap<String, Object> projectMap =
        (LinkedHashMap<String, Object>) context.get("_project");
    Project project =
        Beans.get(ProjectRepository.class).find(Long.parseLong(projectMap.get("id").toString()));

    List<Employee> employeeList = new ArrayList<>();
    if (context.get("employeeSet") == null) {
      String domain = Beans.get(AllocationLineService.class).getEmployeeDomain(project);
      employeeList = Beans.get(EmployeeRepository.class).all().filter(domain).fetch();
    } else {
      for (Map<String, Object> employeeMap :
          (Collection<Map<String, Object>>) context.get("employeeSet")) {
        employeeList.add(
            Beans.get(EmployeeRepository.class)
                .find(Long.parseLong(employeeMap.get("id").toString())));
      }
    }

    List<Period> periodList = new ArrayList<>();
    for (Map<String, Object> periodMap :
        (Collection<Map<String, Object>>) context.get("periodSet")) {
      periodList.add(
          Beans.get(PeriodRepository.class).find(Long.parseLong(periodMap.get("id").toString())));
    }

    boolean initWithPlanningTime =
        Optional.ofNullable(context.get("initWithPlanningTime"))
            .map(it -> ((boolean) it))
            .orElse(false);

    BigDecimal allocated = BigDecimal.ZERO;
    if (context.get("allocated") != null) {
      allocated = new BigDecimal(context.get("allocated").toString());
    }
    Beans.get(AllocationLineService.class)
        .addAllocationLines(project, employeeList, periodList, allocated, initWithPlanningTime);
    response.setCanClose(true);
  }

  public void getTotalAllocatedTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Long projectId = Long.valueOf(((Map) request.getContext().get("project")).get("id").toString());
    Project project = Beans.get(ProjectRepository.class).find(1L);
    LocalDate fromDate = LocalDate.parse((CharSequence) request.getContext().get("fromDate"));
    LocalDate toDate = LocalDate.parse((CharSequence) request.getContext().get("toDate"));
    AllocationLineComputeService allocationLineComputeService =
        Beans.get(AllocationLineComputeService.class);

    BigDecimal plannedTime =
        allocationLineComputeService.computePlannedTime(fromDate, toDate, null, project);

    Map<String, Object> data = new HashMap<>();
    data.put("plannedTime", plannedTime);

    response.setData(List.of(data));
  }
}
