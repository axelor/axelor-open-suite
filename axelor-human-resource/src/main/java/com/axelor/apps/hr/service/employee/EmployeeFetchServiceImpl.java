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
package com.axelor.apps.hr.service.employee;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.db.Query;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class EmployeeFetchServiceImpl implements EmployeeFetchService {

  protected EmployeeRepository employeeRepository;

  @Inject
  public EmployeeFetchServiceImpl(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
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
}
