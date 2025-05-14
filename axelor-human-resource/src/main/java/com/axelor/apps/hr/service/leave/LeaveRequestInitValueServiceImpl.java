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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import java.util.Optional;

public class LeaveRequestInitValueServiceImpl implements LeaveRequestInitValueService {
  @Override
  public void initLeaveRequest(LeaveRequest leaveRequest) {
    User user = AuthUtils.getUser();
    Employee employee = Optional.ofNullable(user).map(User::getEmployee).orElse(null);
    leaveRequest.setEmployee(employee);
    leaveRequest.setCompany(getCompany(user, employee));
  }

  protected Company getCompany(User user, Employee employee) {
    Company company =
        Optional.ofNullable(employee)
            .map(Employee::getMainEmploymentContract)
            .map(EmploymentContract::getPayCompany)
            .orElse(null);
    if (company == null && user != null) {
      return user.getActiveCompany();
    }
    return company;
  }
}
