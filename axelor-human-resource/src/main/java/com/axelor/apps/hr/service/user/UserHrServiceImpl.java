/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.user;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EventsPlanning;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class UserHrServiceImpl implements UserHrService {

  @Inject UserRepository userRepo;

  @Transactional
  public void createEmployee(User user) {
    if (user.getPartner() == null) {
      Beans.get(UserService.class).createPartner(user);
    }

    General config = Beans.get(GeneralService.class).getGeneral();

    Employee employee = new Employee();
    employee.setContactPartner(user.getPartner());
    employee.setTimeLoggingPreferenceSelect(config.getTimeLoggingPreferenceSelect());
    employee.setDailyWorkHours(config.getDailyWorkHours());
    employee.setNegativeValueLeave(config.getAllowNegativeLeaveEmployees());

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
}
