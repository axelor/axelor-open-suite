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
package com.axelor.apps.hr.service.employee;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.Query;
import com.axelor.utils.helpers.QueryBuilder;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class EmployeeFetchServiceImpl implements EmployeeFetchService {

  protected EmployeeRepository employeeRepository;
  protected AppBaseService appBaseService;

  @Inject
  public EmployeeFetchServiceImpl(
      EmployeeRepository employeeRepository, AppBaseService appBaseService) {
    this.employeeRepository = employeeRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  public List<Employee> getEmployees(HrBatch hrBatch) {
    Company company = hrBatch.getCompany();
    String query = getFilters(hrBatch);
    Query<Employee> employeeQuery = employeeRepository.all().filter(query);
    if (company != null) {
      employeeQuery.bind("company", company);
    }
    return employeeQuery.fetch();
  }

  protected String getFilters(HrBatch hrBatch) {
    Company company = hrBatch.getCompany();
    Set<Employee> employeeSet = hrBatch.getEmployeeSet();
    Set<WeeklyPlanning> weeklyPlanningSet = hrBatch.getPlanningSet();
    List<String> query = Lists.newArrayList();

    if (CollectionUtils.isNotEmpty(employeeSet)) {
      String employeeIds = StringHelper.getIdListString(employeeSet);
      query.add("self.id IN (" + employeeIds + ")");
    }

    if (CollectionUtils.isNotEmpty(employeeSet) && CollectionUtils.isNotEmpty(weeklyPlanningSet)) {
      String planningIds = StringHelper.getIdListString(weeklyPlanningSet);
      query.add("self.weeklyPlanning.id IN (" + planningIds + ")");
    }

    if (company != null) {
      query.add("self.mainEmploymentContract.payCompany = :company");
    }

    return Joiner.on(" AND ").join(query);
  }

  @Override
  public List<Employee> getInvitedCollaborators(LocalDate expenseDate, Employee employeeToRemove) {
    QueryBuilder<Employee> queryBuilder = getInvitedCollaborators(expenseDate);
    if (employeeToRemove != null) {
      queryBuilder
          .add("self.id != :excludedEmployee")
          .bind("excludedEmployee", employeeToRemove.getId());
    }
    return queryBuilder.build().fetch();
  }

  @Override
  public List<Employee> filterInvitedCollaborators(
      List<Long> invitedCollaboratorList, LocalDate expenseDate) {
    if (invitedCollaboratorList == null || invitedCollaboratorList.isEmpty()) {
      return new ArrayList<>();
    }
    return getInvitedCollaborators(expenseDate)
        .add("self.id IN :invitedCollaboratorList")
        .bind("invitedCollaboratorList", invitedCollaboratorList)
        .build()
        .fetch();
  }

  protected QueryBuilder<Employee> getInvitedCollaborators(LocalDate expenseDate) {
    return QueryBuilder.of(Employee.class)
        .add(
            "self.user.blocked = false AND self.hireDate <= :expenseDate"
                + " AND (self.leavingDate=null OR self.leavingDate >= :expenseDate)"
                + " AND (self.user.expiresOn is null OR self.user.expiresOn> :currentDate)"
                + " AND self.mainEmploymentContract.payCompany IN :companySet")
        .bind("expenseDate", expenseDate)
        .bind("companySet", AuthUtils.getUser().getCompanySet())
        .bind("currentDate", appBaseService.getTodayDate(null));
  }
}
