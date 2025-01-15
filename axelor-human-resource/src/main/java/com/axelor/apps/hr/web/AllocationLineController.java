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

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.allocation.AllocationLineService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    if (request.getContext().get("project") == null) {
      return;
    }
    LinkedHashMap<String, Object> projectMap =
        (LinkedHashMap<String, Object>) request.getContext().get("project");
    Project project =
        Beans.get(ProjectRepository.class).find(Long.parseLong(projectMap.get("id").toString()));
    response.setAttr(
        "$employeeSet",
        "domain",
        Beans.get(AllocationLineService.class).getEmployeeDomain(project));
  }

  public void addAllocationLine(ActionRequest request, ActionResponse response) {
    AllocationLine allocationLine = request.getContext().asType(AllocationLine.class);
    Beans.get(AllocationLineService.class)
        .createOrUpdateAllocationLine(
            allocationLine.getProject(),
            allocationLine.getEmployee(),
            allocationLine.getPeriod(),
            allocationLine.getAllocated());
    response.setCanClose(true);
  }

  @SuppressWarnings("unchecked")
  public void addAllocationLines(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    if (context.get("project") == null) {
      return;
    }
    LinkedHashMap<String, Object> projectMap =
        (LinkedHashMap<String, Object>) context.get("project");
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

    BigDecimal allocated = BigDecimal.ZERO;
    if (context.get("allocated") != null) {
      allocated = new BigDecimal(context.get("allocated").toString());
    }
    Beans.get(AllocationLineService.class)
        .addAllocationLines(project, employeeList, periodList, allocated);
    response.setCanClose(true);
  }

  @SuppressWarnings("unchecked")
  public void removeAllocationLines(ActionRequest request, ActionResponse response) {
    List<Integer> allocationLineIds = (List<Integer>) request.getContext().get("_ids");
    Beans.get(AllocationLineService.class).removeAllocationLines(allocationLineIds);
    response.setReload(true);
  }
}
