package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class EmployeeComputeAvailableLeaveServiceImpl
    implements EmployeeComputeAvailableLeaveService {

  @Override
  public BigDecimal computeAvailableLeaveQuantityForActiveUser(
      Employee employee, LeaveReason leaveReason) {
    if (employee == null
        || leaveReason == null
        || leaveReason.getLeaveReasonTypeSelect()
            == LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS
        || CollectionUtils.isEmpty(employee.getLeaveLineList())) {
      return BigDecimal.ZERO;
    }

    return employee.getLeaveLineList().stream()
        .filter(ll -> ll.getLeaveReason().equals(leaveReason))
        .map(LeaveLine::getQuantity)
        .findFirst()
        .orElse(BigDecimal.ZERO);
  }
}
