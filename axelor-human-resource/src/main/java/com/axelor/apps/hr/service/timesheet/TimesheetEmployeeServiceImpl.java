package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import java.util.Optional;

public class TimesheetEmployeeServiceImpl implements TimesheetEmployeeService {
  @Override
  public Employee getEmployee(Project project) throws AxelorException {
    User user =
        Optional.ofNullable(project).map(Project::getAssignedTo).orElse(AuthUtils.getUser());
    Employee employee = Optional.ofNullable(user).map(User::getEmployee).orElse(null);

    if (user == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_CREATE_NO_USER_ERROR));
    }

    if (employee == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE),
          user.getName());
    }
    return employee;
  }
}
