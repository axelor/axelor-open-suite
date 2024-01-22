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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.time.LocalDate;

public class EmployeeComputeStatusServiceImpl implements EmployeeComputeStatusService {

  protected static final String NEW_EMPLOYEE = "new";
  protected static final String FORMER_EMPLOYEE = "former";
  protected static final String ACTIVE_EMPLOYEE = "active";

  protected AppBaseService appBaseService;

  @Inject
  public EmployeeComputeStatusServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public String getEmployeeStatus(Employee employee) {
    String employeeStatus = null;
    LocalDate leavingDate = employee.getLeavingDate();
    LocalDate hireDate = employee.getHireDate();

    if (leavingDate == null && hireDate == null) {
      return employeeStatus;
    }

    LocalDate today =
        appBaseService.getTodayDate(
            employee.getUser() != null
                ? employee.getUser().getActiveCompany()
                : AuthUtils.getUser().getActiveCompany());

    if (employee.getLeavingDate() == null
        && employee.getHireDate() != null
        && employee.getHireDate().compareTo(today.minusDays(30)) > 0) {
      employeeStatus = NEW_EMPLOYEE;
    } else if (leavingDate != null && leavingDate.compareTo(today) < 0) {
      employeeStatus = FORMER_EMPLOYEE;
    } else {
      employeeStatus = ACTIVE_EMPLOYEE;
    }
    return employeeStatus;
  }
}
