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
