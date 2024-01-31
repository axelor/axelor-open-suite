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
