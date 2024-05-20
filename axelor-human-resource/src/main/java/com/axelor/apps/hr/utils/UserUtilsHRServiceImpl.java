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
package com.axelor.apps.hr.utils;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class UserUtilsHRServiceImpl implements UserUtilsHRService {

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeLinkedUser(User user) {
    if (user.getEmployee() != null) {
      return;
    }
    EmployeeHRRepository employeeRepository = Beans.get(EmployeeHRRepository.class);
    Employee employee = employeeRepository.find(user.getEmployee().getId());
    if (employee != null) {
      employee.setUser(null);
      employeeRepository.save(employee);
    }
  }
}
