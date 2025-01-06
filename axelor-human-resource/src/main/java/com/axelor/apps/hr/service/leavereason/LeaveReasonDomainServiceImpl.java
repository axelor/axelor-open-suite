package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LeaveReasonDomainServiceImpl implements LeaveReasonDomainService {

  protected final LeaveReasonRepository leaveReasonRepository;

  @Inject
  public LeaveReasonDomainServiceImpl(LeaveReasonRepository leaveReasonRepository) {
    this.leaveReasonRepository = leaveReasonRepository;
  }

  @Override
  public String getLeaveReasonDomain(LeaveReason leaveReason, Employee employee) {
    return "self.id IN (" + StringHelper.getIdListString(getLeaveReasons(employee)) + ")";
  }

  protected Set<LeaveReason> getLeaveReasons(Employee employee) {
    StringBuilder filter = new StringBuilder("self.leaveReasonTypeSelect = 2");
    if (employee == null) {
      return new HashSet<>(leaveReasonRepository.all().filter(filter.toString()).fetch());
    }

    Optional.ofNullable(AuthUtils.getUser())
        .map(User::getEmployee)
        .ifPresent(userEmployee -> filter.append(" AND self.selectedByMgtOnly IS FALSE"));
    Set<LeaveReason> leaveReasons =
        new HashSet<>(leaveReasonRepository.all().filter(filter.toString()).fetch());
    leaveReasons.addAll(
        employee.getLeaveLineList().stream()
            .map(LeaveLine::getLeaveReason)
            .collect(Collectors.toList()));

    return leaveReasons;
  }
}
