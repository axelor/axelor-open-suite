/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;

@Singleton
public class UserHrController {

  public void createEmployee(ActionRequest request, ActionResponse response) {
    User user =
        Beans.get(UserRepository.class).find(request.getContext().asType(User.class).getId());
    Beans.get(UserHrService.class).createEmployee(user);

    response.setReload(true);
  }

  public void createUser(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    User user = context.asType(User.class);
    EmployeeRepository employeeRepository = Beans.get(EmployeeRepository.class);

    if (context.containsKey("_id")) {
      Object employeeId = context.get("_id");
      if (employeeId != null) {
        Employee employee = employeeRepository.find(Long.parseLong(employeeId.toString()));
        Beans.get(UserHrService.class).createUserFromEmployee(user, employee);
        response.setCanClose(true);
      }
    }
  }
}
