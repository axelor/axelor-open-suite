/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.web;

import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;

@Singleton
public class UserHrController {

  @Transactional
  public void createEmployee(ActionRequest request, ActionResponse response) {
    User user =
        Beans.get(UserRepository.class).find(request.getContext().asType(User.class).getId());
    Beans.get(UserHrService.class).createEmployee(user);

    response.setReload(true);
  }

  @Transactional
  public void createUser(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    User user = context.asType(User.class);
    EmployeeRepository employeeRepository = Beans.get(EmployeeRepository.class);

    User employeeUser = new User();
    employeeUser.setActivateOn(user.getActivateOn());
    employeeUser.setExpiresOn(user.getExpiresOn());
    employeeUser.setCode(user.getCode());
    employeeUser.setGroup(user.getGroup());

    if (context.containsKey("_id")) {
      Object employeeId = context.get("_id");
      if (employeeId != null) {
        Employee employee = employeeRepository.find(Long.parseLong(employeeId.toString()));
        employeeUser.setEmployee(employeeRepository.find(employee.getId()));

        if (employee.getContactPartner() != null) {
          String employeeName = employee.getContactPartner().getName();
          if (employee.getContactPartner().getFirstName() != null) {
            employeeName += " " + employee.getContactPartner().getFirstName();
          }
          employeeUser.setName(employeeName);
          if (employee.getContactPartner().getEmailAddress() != null) {
            employeeUser.setEmail(employee.getContactPartner().getEmailAddress().getAddress());
          }
        }

        if (employee.getMainEmploymentContract() != null) {
          employeeUser.setActiveCompany(employee.getMainEmploymentContract().getPayCompany());
        }

        List<EmploymentContract> contractList = employee.getEmploymentContractList();
        if (contractList != null && !contractList.isEmpty()) {
          for (EmploymentContract employmentContract : contractList) {
            employeeUser.addCompanySetItem(employmentContract.getPayCompany());
          }
        }
        CharSequence password = Beans.get(UserService.class).generateRandomPassword();
        employeeUser.setPassword(password.toString());
        employee.setUser(employeeUser);
      }
    }

    Beans.get(UserRepository.class).save(employeeUser);
    response.setCanClose(true);
  }
}
