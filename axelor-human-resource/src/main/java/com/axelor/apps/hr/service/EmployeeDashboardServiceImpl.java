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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeDashboardServiceImpl implements EmployeeDashboardService {
  protected ProjectTaskRepository projectTaskRepo;
  protected ProjectRepository projectRepo;
  protected EmployeeRepository employeeRepository;
  protected AppBaseService appBaseService;

  @Inject
  public EmployeeDashboardServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      ProjectRepository projectRepo,
      EmployeeRepository employeeRepository,
      AppBaseService appBaseService) {
    this.projectTaskRepo = projectTaskRepo;
    this.projectRepo = projectRepo;
    this.employeeRepository = employeeRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  public List<Long> getFilteredEmployeeIds(Project project) {
    String filter =
        " (self.hireDate is not null and self.hireDate <= :todayDate) "
            + "and (self.leavingDate is null or self.leavingDate >= :todayDate)";
    List<Employee> projectEmployeeList = null;
    if (project != null) {
      projectEmployeeList =
          project.getMembersUserSet().stream().map(User::getEmployee).collect(Collectors.toList());
      filter = filter + " and self in :projectEmployeeList";
    }
    LocalDate todayDate =
        appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));

    Query<Employee> employeeQuery =
        employeeRepository.all().filter(filter).bind("todayDate", todayDate);

    if (projectEmployeeList != null) {
      employeeQuery.bind("projectEmployeeList", projectEmployeeList);
    }
    return employeeQuery.select("id").fetch(0, 0).stream()
        .map(m -> (Long) m.get("id"))
        .collect(Collectors.toList());
  }
}
