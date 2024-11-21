package com.axelor.apps.hr.service.extra.hours;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ExtraHoursDomainServiceImpl implements ExtraHoursDomainService {

  protected final EmployeeRepository employeeRepository;

  @Inject
  public ExtraHoursDomainServiceImpl(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @Override
  public String getEmployeeDomain() {
    return "self.id IN (" + StringHelper.getIdListString(getEmployees()) + ")";
  }

  protected Set<Employee> getEmployees() {
    Set<Employee> employees = new HashSet<>();

    User user = AuthUtils.getUser();
    Employee employee = Optional.ofNullable(user).map(User::getEmployee).orElse(null);

    if (user != null) {
      employees.addAll(
          employeeRepository.all().filter("self.managerUser = :user").bind("user", user).fetch());
    }

    if (employee != null) {
      employees.add(employee);

      if (employee.getHrManager()) {
        Partner contactPartner = employee.getContactPartner();
        Set<Company> companySet = contactPartner.getCompanySet();
        if (CollectionUtils.isNotEmpty(companySet)) {
          for (Company company : companySet) {
            employees.addAll(
                employeeRepository
                    .all()
                    .filter(":company MEMBER OF self.contactPartner.companySet")
                    .bind("company", company)
                    .fetch());
          }
        }
      }
    }

    return employees;
  }
}
