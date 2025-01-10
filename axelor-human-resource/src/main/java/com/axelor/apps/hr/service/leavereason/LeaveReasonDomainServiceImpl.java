package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeaveReasonDomainServiceImpl implements LeaveReasonDomainService {

  protected final LeaveReasonRepository leaveReasonRepository;

  @Inject
  public LeaveReasonDomainServiceImpl(LeaveReasonRepository leaveReasonRepository) {
    this.leaveReasonRepository = leaveReasonRepository;
  }

  @Override
  public String getLeaveReasonDomain(Employee employee) {
    StringBuilder filter =
        new StringBuilder(
            String.format(
                "self.leaveReasonTypeSelect = %s",
                LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS));

    if (employee == null) {
      return filter.toString();
    }

    Employee userEmployee =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getEmployee).orElse(null);

    List<LeaveReason> leaveLineLeaveReasonList = getLeaveLineLeaveReasons(employee, userEmployee);

    if (userEmployee != null && !userEmployee.getHrManager()) {
      filter.append(" AND self.selectedByMgtOnly IS FALSE");
      filter
          .append(" OR self.id IN (")
          .append(StringHelper.getIdListString(leaveLineLeaveReasonList))
          .append(")");
    } else {
      filter
          .append(" OR self.id IN (")
          .append(StringHelper.getIdListString(leaveLineLeaveReasonList))
          .append(")");
    }
    return filter.toString();
  }

  protected List<LeaveReason> getLeaveLineLeaveReasons(Employee employee, Employee userEmployee) {
    List<LeaveReason> leaveLineLeaveReasonList;

    if (userEmployee != null && !userEmployee.getHrManager()) {
      leaveLineLeaveReasonList =
          employee.getLeaveLineList().stream()
              .map(LeaveLine::getLeaveReason)
              .filter(leaveReason -> !leaveReason.getSelectedByMgtOnly())
              .collect(Collectors.toList());
    } else {
      leaveLineLeaveReasonList =
          employee.getLeaveLineList().stream()
              .map(LeaveLine::getLeaveReason)
              .collect(Collectors.toList());
    }
    return leaveLineLeaveReasonList;
  }
}
