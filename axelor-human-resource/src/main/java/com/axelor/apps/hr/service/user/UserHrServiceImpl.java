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
package com.axelor.apps.hr.service.user;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppLeave;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class UserHrServiceImpl implements UserHrService {

  @Inject UserRepository userRepo;

  @Inject private AppHumanResourceService appHumanResourceService;

  @Transactional
  public void createEmployee(User user) {
    if (user.getPartner() == null) {
      Beans.get(UserService.class).createPartner(user);
    }

    AppBase appBase = appHumanResourceService.getAppBase();
    AppLeave appLeave = appHumanResourceService.getAppLeave();

    Employee employee = new Employee();
    employee.setContactPartner(user.getPartner());
    employee.setTimeLoggingPreferenceSelect(appBase.getTimeLoggingPreferenceSelect());
    employee.setDailyWorkHours(appBase.getDailyWorkHours());
    employee.setNegativeValueLeave(appLeave.getAllowNegativeLeaveEmployees());

    EventsPlanning planning = null;
    Company company = user.getActiveCompany();
    if (company != null) {
      HRConfig hrConfig = company.getHrConfig();
      if (hrConfig != null) {
        planning = hrConfig.getPublicHolidayEventsPlanning();
      }
    }
    employee.setPublicHolidayEventsPlanning(planning);

    employee.setUser(user);
    Beans.get(EmployeeRepository.class).save(employee);

    user.setEmployee(employee);
    userRepo.save(user);
  }

  @Transactional
  public Company getPayCompany(User user) {
    Company payCompany = null;
    if (user.getEmployee() != null
        && user.getEmployee().getMainEmploymentContract() != null
        && user.getEmployee().getMainEmploymentContract().getPayCompany() != null) {
      payCompany = user.getEmployee().getMainEmploymentContract().getPayCompany();
    } else if (user.getActiveCompany() != null) {
      payCompany = user.getActiveCompany();
    }
    return payCompany;
  }

  @Override
  public Product getTimesheetProduct(Employee employee) {

    User user = employee.getUser();
    if (user == null || user.getActiveCompany() == null) {
      return null;
    }

    Product product = null;
    HRConfig hrConfig = user.getActiveCompany().getHrConfig();
    if (hrConfig != null && hrConfig.getUseUniqueProductForTimesheet()) {
      product = hrConfig.getUniqueTimesheetProduct();
    }

    if (product == null && employee != null) {
      product = employee.getProduct();
    }

    return product;
  }
}
